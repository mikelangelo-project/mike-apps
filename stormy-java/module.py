from osv.modules import api

api.require('java')

default = api.run_java(
        jvm_args=['-Dsupervisor=osv'],
        classpath=['/stormy-java'], 
        args=['Supervisor', '5'])
