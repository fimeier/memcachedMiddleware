#!/bin/bash

if [ $1 == 'readme' ]
then
    git add README.md
    git commit -m "readme"
    git push -u
else
    git add .
    git commit -m "Backup all"
    git push -u
fi

