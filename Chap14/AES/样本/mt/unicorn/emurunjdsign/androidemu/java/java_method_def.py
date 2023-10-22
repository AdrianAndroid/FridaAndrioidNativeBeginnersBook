import logging
from androidemu.java.jvm_id_conter import *

logger = logging.getLogger(__name__)

class JavaMethodDef:

    def __init__(self, func_name, func, name, signature, native, args_list=None, modifier=None, ignore=None,jvm_id = None):

        if jvm_id == None:
            self.jvm_id = next_method_id()
        else:
            self.jvm_id = jvm_id
        logger.debug("JavaMethodDef name =%s,jvm_id = %s" % (name,self.jvm_id))                                                
        self.func_name = func_name
        self.func = func
        self.name = name
        self.signature = signature
        self.native = native
        self.native_addr = None
        self.args_list = args_list
        self.modifier = modifier
        self.ignore = ignore


def java_method_def(name, signature, native=False, args_list=None, modifier=None, ignore=False, jvm_id=None):
    def java_method_def_real(func):
        def native_wrapper(self, emulator, *argv):
            return emulator.call_native(
                native_wrapper.jvm_method.native_addr,
                emulator.java_vm.jni_env.address_ptr,  # JNIEnv*
                0xFA,    # this, TODO: Implement proper "this", a reference to the Java object inside which this native
                         # method has been declared in
                *argv  # Extra args.
            )
        #
        def normal_wrapper(*args, **kwargs):
            result = func(*args, **kwargs)
            return result
        #
        wrapper = native_wrapper if native else normal_wrapper
        wrapper.jvm_method = JavaMethodDef(func.__name__, wrapper, name, signature, native,
                                           args_list=args_list,
                                           modifier=modifier,
                                           ignore=ignore,
                                           jvm_id=jvm_id)
        return wrapper
    #
    return java_method_def_real
#
