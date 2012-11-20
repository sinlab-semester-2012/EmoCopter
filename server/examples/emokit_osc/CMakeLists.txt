#########################################
#				EMOKIT OSC				#
#########################################

ADD_EXECUTABLE(emokit_osc emokit_osc.cpp)
ADD_DEPENDENCIES(emokit_osc emokit)
TARGET_LINK_LIBRARIES(emokit_osc emokit OSC ${LIBS})
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/include)

SET_TARGET_PROPERTIES(emokit_osc PROPERTIES
	INSTALL_RPATH_USE_LINK_PATH TRUE
	INSTALL_RPATH ${CMAKE_SOURCE_DIR}/lib
	BUILD_WITH_INSTALL_RPATH TRUE )

INSTALL (TARGETS emokit_osc
	RUNTIME DESTINATION bin
)
