'''
Created on 2015-08-19

@author: y136wang
'''

import socket
import logging
import sys
import os
import glob
import time
import urllib

from xmlrpclib import ProtocolError
from robot.libraries.Process import Process
from robot.libraries.Remote import Remote
from robot.libraries.BuiltIn import BuiltIn


class SikuliLibrary(object):

    ROBOT_LIBRARY_SCOPE = 'GLOBAL'

    def __init__(self, port=0, timeout=3.0):
        """
        @port: sikuli java process socket port
        @timeout: Timeout of waiting java process started
        """
        self.logger = self._init_logger()
        self.timeout = float(timeout)
        if int(port) == 0:
            port = self._get_free_tcp_port()
        self.port = port
        self._start_sikuli_java_process()
        self.remote = self._connect_remote_library()

    def _init_logger(self):
        robotLogLevels = {'TRACE': logging.DEBUG/2,
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
        except Exception, err:
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
        jarList = glob.glob(libFolder+os.sep+'*.jar')
        if len(jarList) != 1:
            raise Exception('Sikuli jar package should be exist in lib folder')
        sikuliJar = jarList[0]
        command = 'java -jar '+sikuliJar+' %s ' % str(self.port)+self._get_output_folder()
        process = Process()
        process.start_process(command, shell=True, stdout=self._output_file(), stderr=self._err_file())
        self.logger.info('Start sikuli java process on port %s' % str(self.port))
        self._wait_process_started()
        self.logger.info('Sikuli java process is started')

    def _wait_process_started(self):
        url = "http://127.0.0.1:%s" % str(self.port)
        currentTime = startedTime = time.time()
        started = False
        while((currentTime-startedTime)<self.timeout):
            try:
                urllib.urlopen(url).read()
            except Exception, err:
                currentTime = time.time()
                time.sleep(1.0)
                continue
            started = True
            break
        if not started:
            raise RuntimeError('Start sikuli java process failed!')


    def _output_file(self):
        outputDir = self._get_output_folder()
        outputFile = 'Sikuli_java_stdout_'+str(time.time())+'.txt'
        return outputFile

    def _err_file(self):
        outputDir = self._get_output_folder()
        errFile = 'Sikuli_java_stderr_'+str(time.time())+'.txt'
        return errFile

    def _get_output_folder(self):
        outputDir = os.path.abspath(os.curdir)
        try:
            outputDir = BuiltIn().get_variable_value('${OUTPUTDIR}')
        except Exception, err:
            pass
        return outputDir

    def _connect_remote_library(self):
        remoteUrl = 'http://127.0.0.1:%s' % str(self.port)
        remote = Remote(remoteUrl)
        return remote

    def get_keyword_names(self):
        return self.remote.get_keyword_names()

    def get_keyword_arguments(self, name):
        return self.remote.get_keyword_arguments(name)

    def get_keyword_documentation(self, name):
        return self.remote.get_keyword_documentation(name)

    def run_keyword(self, name, arguments=[], kwargs={}):
        return self.remote.run_keyword(name, arguments, kwargs)


