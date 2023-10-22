from androidemu.java.java_class_def import JavaClassDef
from androidemu.java.java_field_def import JavaFieldDef
from androidemu.java.java_method_def import java_method_def,JavaMethodDef
from androidemu.java.classes.package_manager import PackageManager


class ContextImpl(metaclass=JavaClassDef, jvm_name='android/app/ContextImpl'):

    def __init__(self):
        self.__pkg_mgr = PackageManager()
    #

    @java_method_def(name='getPackageManager', signature='()Landroid/content/pm/PackageManager;', native=False)
    def getPackageManager(self, emu):
        return self.__pkg_mgr
    #
#