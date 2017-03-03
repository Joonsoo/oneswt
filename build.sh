VERSION_NUM=4.6.2

rm -rf bin build maven

javac src/main/java/com/giyeok/oneswt/SwtLoader.java -cp lib

mkdir -p bin/com/giyeok/oneswt
mv src/main/java/com/giyeok/oneswt/SwtLoader.class bin/com/giyeok/oneswt/SwtLoader.class
cp -R lib/org bin/org

DESTDIR=build
DESTPATH=$DESTDIR/oneswt.jar
mkdir -p $DESTDIR

cd bin
jar cf ../$DESTPATH .

cd ../swt
jar uf ../$DESTPATH ./*-x86.jar
jar uf ../$DESTPATH ./*-x86_64.jar

cd ..

MAVENPATH=maven/com/giyeok/oneswt/com.giyeok.oneswt/$VERSION_NUM

mkdir -p $MAVENPATH
mv $DESTPATH $MAVENPATH/com.giyeok.oneswt-$VERSION_NUM.jar
cp src/oneswt_pom.xml $MAVENPATH/com.giyeok.oneswt-$VERSION_NUM.pom
sed -i.bak "s/VERSION_NUM/${VERSION_NUM}/g" $MAVENPATH/com.giyeok.oneswt-$VERSION_NUM.pom

SWT_MAVENBASE=maven/org/eclipse/swt

mkdir -p $SWT_MAVENBASE

cd swt
for filename in $(ls swt-$VERSION_NUM-*-x86.jar swt-$VERSION_NUM-*-x86_64.jar)
do
ARTIFACT_NAME=${filename%\.jar}
mkdir -p ../$SWT_MAVENBASE/$ARTIFACT_NAME/$VERSION_NUM
cp $filename ../$SWT_MAVENBASE/$ARTIFACT_NAME/$VERSION_NUM/$ARTIFACT_NAME-$VERSION_NUM.jar
cp $ARTIFACT_NAME-sources.jar ../$SWT_MAVENBASE/$ARTIFACT_NAME/$VERSION_NUM/$ARTIFACT_NAME-$VERSION_NUM-sources.jar
POM_PATH=../$SWT_MAVENBASE/$ARTIFACT_NAME/$VERSION_NUM/$ARTIFACT_NAME-$VERSION_NUM.pom
cp ../src/swt_pom.xml $POM_PATH
sed -i.bak "s/VERSION_NUM/${VERSION_NUM}/g" $POM_PATH
sed -i.bak "s/ARTIFACT_NAME/${ARTIFACT_NAME}/g" $POM_PATH
done
