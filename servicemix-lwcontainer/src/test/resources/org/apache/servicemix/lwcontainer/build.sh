#/!bin/sh
rm au1-src/*.zip
jar -cvf au1-src/su1.zip -C su1-src/ .
jar -cvf au1.zip -C au1-src/ .
jar -cvf component.zip -C component-src/ .
