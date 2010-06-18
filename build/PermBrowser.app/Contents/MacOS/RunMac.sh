#!/bin/sh
BASEDIR=`dirname $0`
echo sesam Interview is starting up...
exec java -XstartOnFirstThread -Xdock:icon=../Resources/permbrowser.icns -Dorg.eclipse.swt.internal.carbon.smallFonts -Djava.library.path=$BASEDIR/lib -jar $BASEDIR/permbrowser-mac.jar
