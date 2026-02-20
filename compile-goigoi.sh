#!/bin/bash

set -e
set -o pipefail

GOIGOIDIR="$HOME/Develop/goigoi-app"
COMPILER="$GOIGOIDIR/goigoi-compiler/build/libs/goigoi-compiler.jar"

# @param $1 STUDY_LANG
function cleanCompileWithDefaultArgs {
   STUDY_LANG="$1"
   SRCDIR="$GOIGOIDIR/goigoi-xml/voc_$STUDY_LANG"

   if [[ ! -d "$SRCDIR" ]]; then
      2>&1 echo "Directory not accessible: $SRCDIR"
      exit 1
   fi

   DSTDIR="$GOIGOIDIR/app/src/main/assets/voc_${STUDY_LANG}"

   if [[ -d "$DSTDIR" ]]; then
      rm -rf "$DSTDIR"
   fi

   mkdir "$DSTDIR"

   OUTFILE="$GOIGOIDIR/out_${STUDY_LANG}.txt"

   if [[ -f "$OUTFILE" ]]; then
      rm "$OUTFILE"
   fi

   echo "THIS FILE WAS AUTOMATICALLY GENERATED, DO NOT EDIT!" > "$OUTFILE"

   cd "$GOIGOIDIR"
   java -jar "$COMPILER" -d="$SRCDIR" -o="$DSTDIR" | tee -a "$OUTFILE"
   echo | tee -a "$OUTFILE"

   cd "$DSTDIR"/..
   du -shA "voc_$STUDY_LANG/" | tee -a "$OUTFILE"
}

if [[ "$#" == 0 ]]; then
  # No arguments means we're building all available languages and write the outfile.
  cleanCompileWithDefaultArgs ja
else
  # When an argument is given, just call the compiler with those arguments and no side-effects.
   java -jar "$COMPILER" "$@"
fi
