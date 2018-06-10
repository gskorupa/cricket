#!/bin/sh
java --illegal-access=deny --add-modules java.xml.bind --add-modules java.activation --add-opens java.base/java.util=java.xml.bind -jar {{distribution}} -c work/config/cricket.json -s Microsite -r
