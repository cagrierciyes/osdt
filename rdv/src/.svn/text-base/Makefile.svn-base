PROPERTIES="-Denv.J3D_HOME=lib"

all:
	ant ${PROPERTIES} -buildfile ../build.xml compile

install:
	ant ${PROPERTIES} -buildfile ../build.xml jar
	cp ../build/bin/rdv.jar ../bin

clean:
	ant ${PROPERTIES} -buildfile ../build.xml clean
	rm -rf ../build

distclean: clean
	rm -f ../bin/rdv.jar
