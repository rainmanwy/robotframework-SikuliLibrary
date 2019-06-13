"""
Created on 2015-08-19

@author: wang_yang1980@hotmail.com
"""

import socket
import logging
import sys
import os
import glob
import time
import threading
import codecs

try:
    from urllib import urlopen
except ImportError:
    from urllib.request import urlopen
try:
    from xmlrpclib import ProtocolError
except ImportError:
    from xmlrpc.client import ProtocolError
from robot.libraries.Process import Process
from robot.libraries.Remote import Remote
from robot.libraries.BuiltIn import BuiltIn
from .version import VERSION
try:
    from .keywords import KEYWORDS
except ImportError:
    pass


class SikuliLibrary(object):
    ROBOT_LIBRARY_SCOPE = 'GLOBAL'
    ROBOT_LIBRARY_VERSION = VERSION

    def __init__(self, port=0, timeout=3.0, mode='OLD'):
        """
        @port: sikuli java process socket port
        @timeout: Timeout of waiting java process started
        @mode: if set as 'DOC',  will stop java process automatically, 
               if set as 'PYTHON', means library is running out of robot environment
               if set as 'CREATE', it is only for mvn package usage, will create keywords.py file
               if set as 'OLD'(default), sikuli java process will be started when library is inited
               if set as 'NEW', user should use 'start_sikuli_process' to start java process
        """
        self.logger = self._init_logger()
        self.timeout = float(timeout)
        self.port = None
        self.remote = None
        self.mode = mode.upper().strip()
        if mode == 'OLD':
            self.start_sikuli_process(port)
        if mode.upper().strip() == 'DOC':
            self.start_sikuli_process()
            self._stop_thread(4)
        elif mode.upper().strip() == 'PYTHON':
            self.connect_sikuli_process(port)
        elif mode.upper().strip() == 'CREATE':
            self._create_keywords_file()
        elif mode.upper().strip() != 'NEW':
            self._check_robot_running()

    def start_sikuli_process(self, port=None):
        """
        This keyword is used to start sikuli java process.
        If library is inited with mode "OLD", sikuli java process is started automatically.
        If library is inited with mode "NEW", this keyword should be used.

        :param port: port of sikuli java process, if value is None or 0, a random free port will be used
        :return: None
        """
        if port is None or int(port) == 0:
            port = self._get_free_tcp_port()
        self.port = port
        start_retries = 0
        started = False
        while start_retries < 5:
            try:
                self._start_sikuli_java_process()
            except RuntimeError as err:
                print('error........%s' % err)
                if self.process:
                    self.process.terminate_process()
                self.port = self._get_free_tcp_port()
                start_retries += 1
                continue
            started = True
            break
        if not started:
            raise RuntimeError('Start sikuli java process failed!')
        self.remote = self._connect_remote_library()

    def connect_sikuli_process(self, port):
        self.port = port
        self.remote = self._connect_remote_library()

    def _create_keywords_file(self):
        keywordDict = {}
        self.start_sikuli_process()
        try:
            keywordList = self.get_keyword_names()
            for keywordName in keywordList:
                keywordDict[keywordName] = {}
                keywordDict[keywordName]['arg'] = self.get_keyword_arguments(keywordName)
                keywordDict[keywordName]['doc'] = self.get_keyword_documentation(keywordName)
            with codecs.open(os.path.join(os.path.abspath(os.path.dirname(__file__)), 'keywords.py'), 'w',
                             encoding='utf-8') as f:
                f.write('# -*- coding: utf-8 -*-\n')
                # keywords = ','.join(['"%s": %s' % (k, keywordDict[k]) for k in keywordDict.keys()])
                f.write('KEYWORDS = %s' % keywordDict )
        finally:
            self._stop_thread(3)

    def _check_robot_running(self):
        try:
            BuiltIn().get_variable_value('${SUITE SOURCE}')
        except Exception as err:
            self.logger.warn('Robot may not running, stop java process: %s' % err)
            self._stop_thread(1)

    def _init_logger(self):
        robotLogLevels = {'TRACE': int(logging.DEBUG / 2),
                          'DEBUG': logging.DEBUG,
                          'INFO': logging.INFO,
                          'HTML': logging.INFO,
                          'WARN': logging.WARN}
        builtIn = BuiltIn()
        handler = logging.StreamHandler(sys.stdout)
        formatter = logging.Formatter('%(message)s')
        handler.setFormatter(formatter)
        logger = logging.getLogger('SikuliLibraryLogger')
        logger.addHandler(handler)
        level = logging.DEBUG
        try:
            logLevel = builtIn.get_variable_value('${LOG_LEVEL}')
            level = robotLogLevels[logLevel]
        except Exception:
            pass
        logger.setLevel(level)
        return logger

    def _get_free_tcp_port(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.bind(('localhost', 0))
        sock.listen(1)
        host, port = sock.getsockname()
        self.logger.debug('Free TCP port is: %d' % port)
        sock.close()
        return port

    def _start_sikuli_java_process(self):
        libFolder = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'lib')
        jarList = glob.glob(libFolder + os.sep + '*.jar')
        if len(jarList) != 1:
            raise Exception('Sikuli jar package should be exist in lib folder')
        sikuliJar = jarList[0]
        java = 'java'
        arguments = ['-jar', sikuliJar, str(self.port), self._get_output_folder()]
        self.process = Process()
        if os.getenv("DISABLE_SIKULI_LOG"):
            self.process.start_process(java, *arguments, shell=True)
        else:
            self.process.start_process(java, *arguments, shell=True, stdout=self._output_file(),
                                       stderr=self._err_file())
        self.logger.info('Start sikuli java process on port %s' % str(self.port))
        self._wait_process_started()
        self.logger.info('Sikuli java process is started')

    def _wait_process_started(self):
        url = "http://127.0.0.1:%s/" % str(self.port)
        currentTime = startedTime = time.time()
        started = False
        while (currentTime - startedTime) < self.timeout:
            try:
                urlopen(url).read()
            except Exception:
                currentTime = time.time()
                time.sleep(1.0)
                continue
            started = True
            break
        if not started:
            raise RuntimeError('Start sikuli java process failed!')

    def _output_file(self):
        outputDir = self._get_output_folder()
        outputFile = 'Sikuli_java_stdout_' + str(time.time()) + '.txt'
        return os.path.join(outputDir, outputFile)

    def _err_file(self):
        outputDir = self._get_output_folder()
        errFile = 'Sikuli_java_stderr_' + str(time.time()) + '.txt'
        return os.path.join(outputDir, errFile)

    def _get_output_folder(self):
        outputDir = os.path.abspath(os.curdir)
        try:
            outputDir = BuiltIn().get_variable_value('${OUTPUTDIR}')
        except Exception:
            pass
        return outputDir

    def _connect_remote_library(self):
        remoteUrl = 'http://127.0.0.1:%s/' % str(self.port)
        remote = Remote(remoteUrl)
        self._test_get_keyword_names(remote)
        return remote

    def _test_get_keyword_names(self, remote):
        currentTime = startedTime = time.time()
        started = False
        while (currentTime - startedTime) < self.timeout:
            try:
                remote.get_keyword_names()
            except Exception as err:
                self.logger.warn("Test get_keyword_names failed! %s" % err)
                currentTime = time.time()
                time.sleep(1.0)
                continue
            started = True
            break
        if not started:
            raise RuntimeError('Failed to get_keyword_names!')

    def get_keyword_names(self):
        if self.mode == 'CREATE':
            return self.remote.get_keyword_names() + ['start_sikuli_process']
        return list(KEYWORDS.keys()) + ['start_sikuli_process']
        # return self.remote.get_keyword_names() + ['start_sikuli_process']

    def get_keyword_arguments(self, name):
        if name == 'start_sikuli_process':
            return ['port=None']
        if self.mode == 'CREATE':
            return self.remote.get_keyword_arguments(name)
        return KEYWORDS[name]['arg']

    def get_keyword_documentation(self, name):
        if name == 'start_sikuli_process':
            return self.start_sikuli_process.__doc__
        elif name == '__intro__':
            return SikuliLibrary.__doc__
        elif name == '__init__':
            return getattr(self, name).__doc__
        if self.mode == 'CREATE':
            return self.remote.get_keyword_documentation(name)
        return KEYWORDS[name]['doc']

    def run_keyword(self, name, arguments=[]):
        if name == 'start_sikuli_process':
            return self.start_sikuli_process(*arguments)
        return self.remote.run_keyword(name, arguments, None)

    def _stop_thread(self, timeout):
        def stop():
            time.sleep(float(timeout))
            self.run_keyword('stop_remote_server')

        thread = threading.Thread(target=stop, args=())
        thread.start()
