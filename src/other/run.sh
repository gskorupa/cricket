#!/bin/sh
java --illegal-access=deny --add-modules java.xml.bind --add-modules java.activation -jar {{distribution}} -c work/config/cricket.json -s Microsite -r
#java -jar {{distribution}} -c work/config/cricket.json -s Microsite -r