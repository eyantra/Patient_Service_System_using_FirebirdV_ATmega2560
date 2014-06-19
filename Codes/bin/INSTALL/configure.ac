#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.57)
AC_INIT(PSS, 1.0, rohit.kumar.saraf@gmail.com)

AC_SUBST([CONFIG_PREFIX],["config"])

AC_CHECK_PROGS(JAVA,java ,[no],[path = $PATH])
[if    test "$JAVA" = "no"]
[then]
   [echo "Java not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
   [echo "Please install java using sudo apt-get install default-jdk (on ubuntu systems) or go to the Oracle website to download the latest version".]
   [sudo apt-get install -y default-jdk]
[else]
   [echo "Found java as $JAVA."]
[fi]

AC_CHECK_PROGS(JAVAC,javac ,[no],[path = $PATH])
[if    test "$JAVAC" = "no"]
[then]
   [echo "Java compiler javac not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
   [echo "Please install java using sudo apt-get install default-jdk (on ubuntu systems) or go to the Oracle website to download the latest version".]
   [sudo apt-get install -y default-jdk]
[else]
   [echo "Found javac as $JAVAC."]
[fi]

AC_CHECK_PROGS(PYTHON,python ,[no],[path = $PATH])
[if    test "$PYTHON" = "no"]
[then]
   [echo "Python not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
   [exit 1]
[else]
   [echo "Found python as $PYTHON."]
[fi]

AC_CHECK_PROGS(MYSQL,mysql ,[no],[path = $PATH])
[if    test "$MYSQL" = "no"]
[then]
   [echo "MySQL not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
	[sudo apt-get install -y mysql-server mysql-client]
[else]
   [echo "Found mysql as $MYSQL."]
[fi]

AC_CHECK_PROGS(AVRDUDE,avrdude,[no],[path = $PATH])
[if    test "$AVRDUDE" = "no"]
[then]
   [echo "avrdude gcc-avr avr-libc not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
	[sudo apt-get install -y avrdude]
[else]
   [echo "Found avrdude as $AVRDUDE."]
[fi]


AC_CHECK_PROGS(AVRGCC,avr-gcc,[no],[path = $PATH])
[if    test "$AVRGCC" = "no"]
[then]
   [echo "gcc-avr not found in the search path. Please ensure that it is"]
   [echo "installed and its directory is included in the search path".]
	[sudo apt-get install -y gcc-avr avr-libc]
[else]
   [echo "Found avrdude as $AVRGCC."]
[fi]

AC_CHECK_LIB([l], [main])
AC_HEADER_STDC
AC_SUBST([JAVA],["$JAVA"])
AC_SUBST([JAVAC],["$JAVAC"])
AC_SUBST([PYTHON],["$PYTHON"])
AC_SUBST([AVRDUDE],["$AVRDUDE"])
AC_SUBST([AVRGCC],["$AVRGCC"])
AC_SUBST([AVRLIBC],["$AVRLIBC"])

AC_CONFIG_FILES([Makefile])

AC_OUTPUT
