#!/bin/sh

ant javarifier |\
    grep Stub | grep -v jtest | grep -v javagrok |\
    awk '{ print $3 }' | sed 's:\.:/:g' | sort |\
    sed 's:$:.java:' > ~/research/checker-framework/checkers/jdk/javari/stubs.txt
cd ~/research/checker-framework/checkers/jdk/javari/src
find `cat ../stubs.txt ` -empty
