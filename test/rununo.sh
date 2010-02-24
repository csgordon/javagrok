#!/bin/bash

ant compile
cp -R dist/classes/* ../uno/bin/
cd ../uno
./clean
./build
./uno ../uno-result ../test/file.list
rm -R ./bin/*
cd ../test

