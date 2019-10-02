#!/bin/bash


if [ $1 == 'readme' ]
then
    git add README.md
    git commit -m "$2"
    git push -u
fi

if [ $1 == 'all' ]
then
    git add .
    git commit -m "$2"
    git push -u
fi

