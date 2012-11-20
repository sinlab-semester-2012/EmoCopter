#!/bin/sh

if [ ${PWD##*/} = "emokit" ] ; then echo "Cleaning up";
else echo "Wrong directory. exiting"; exit 1;
fi

# clean main directory
rm -r CMakeFiles/
rm CMakeCache.txt cmake_install.cmake install_manifest.txt Makefile

# clean lib directory
cd lib/
rm libemokit.a libOSC.so
cd ../

# clean examples/emokitd directory
cd examples/emokitd/
rm -r CMakeFiles/
rm cmake_install.cmake Makefile
cd ../../

# clean examples/emokit_osc directory
cd examples/emokit_osc/
rm -r CMakeFiles/
rm cmake_install.cmake Makefile
cd ../../

# clean src directory
cd src/
rm -r CMakeFiles/
rm cmake_install.cmake Makefile
cd ../

# clean include/oscpack directory
cd include/oscpack/
rm -r CMakeFiles/
rm cmake_install.cmake Makefile
cd ../../

# clean bin directory
notDone=true
while($notDone); do
	read -p "Do you wish to remove built files from bin/ also? [y/n] " answer
	case $answer in
		[Yy]* ) notDone=false; rm -r bin/;;
		[Nn]* ) notDone=false;;
	esac
done

echo "Done"
exit 0
