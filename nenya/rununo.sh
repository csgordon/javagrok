#!/bin/bash

ant compile
cp -R dist/classes/* ../uno/bin/
cd ../uno
./clean
./build
./uno ../uno-result ../nenya/file.list
rm -R ./bin/*
cd ../nenya

