'''
Created on 2015/12/10

Author: by wang_yang1980@hotmail.com
'''

from distutils.core import setup

from os.path import abspath, dirname, join
execfile(join(dirname(abspath(__file__)), 'target', 'src', 'SikuliLibrary', 'version.py'))


DESCRIPTION = """
Sikuli Robot Framework Library provide keywords for Robot Framework to test UI through Sikuli.

Notes: SikuliLibrary.jar file is OS dependent. The version for Windows 64bit is included.
If target OS is not Windows, please get source code from GITHUB, and use Maven to build
SikuliLibrary.jar on target OS, and replace the jar file in 'lib' folder.
"""[1:-1]
CLASSIFIERS = """
Operating System :: OS Independent
Programming Language :: Python
Programming Language :: Java
Topic :: Software Development :: Testing
"""[1:-1]

setup(name         = 'robotframework-SikuliLibrary',
      version      = VERSION,
      description  = 'Sikuli library for Robot Framework',
      long_description = DESCRIPTION,
      author       = 'Wang Yang',
      author_email = 'wang_yang1980@hotmail.com',
      url          = 'https://github.com/rainmanwy/robotframework-SikuliLibrary',
      license      = 'Apache License 2.0',
      keywords     = 'robotframework testing testautomation sikuli UI',
      platforms    = 'any',
      classifiers  = CLASSIFIERS.splitlines(),
      package_dir  = {'' : 'target/src'},
      packages     = ['SikuliLibrary'],
      package_data = {'SikuliLibrary': ['lib/*.jar',
                                          ]},
      )