#!/bin/bash

ant compile
cp -R dist/classes/* ../uno/bin/
cd ../uno
./build
./rununo.sh
rm -R ./bin/*
cd ../test

