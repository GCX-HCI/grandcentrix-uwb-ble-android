#!/bin/bash

COLOR_RED='\033[31m'
COLOR_GREEN='\033[32m'
COLOR_YELLOW='\033[33m'
COLOR_HIGHLIGHT='\033[36m'
COLOR_NONE='\033[0m'

PREFIX_LOG_ERROR="${COLOR_RED}>>>${COLOR_NONE} "
PREFIX_LOG_CHAPTER="\033[34m>>>${COLOR_NONE} "

PATH_OF_SCRIPT=${0}

REGEX_PACKAGE_NAME="^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]$"
SEPARATOR_FOLDER=/
PACKAGE_OLD="net.grandcentrix.scaffold"
SUBPATH_PACKAGE_OLD="kotlin/${PACKAGE_OLD//[.]/$SEPARATOR_FOLDER}"

SKIP_DELETION=false

OPTIONS_SED="-i"
if [[ "$OSTYPE" == "darwin"* ]]; then
  # BSD basing versions of sed do require other parameter than UNIX based ones.
  # MacOS's sed is based on BSD so we have to add those parameters here.
  OPTIONS_SED="-i ''"
fi

PATH_FOLDER_SRC=app/src

step () {
    name=$1
    command_name=$2
    command=$3

    printf "%b%s%b - %s: …" "${COLOR_HIGHLIGHT}" "$name" "${COLOR_NONE}" "$command_name"

    result=$(eval "$command" 2>&1)
    resultCode=$?
    if [ $resultCode == 0 ]; then
        printf "\b%bdone%b\n" "${COLOR_GREEN}" "${COLOR_NONE}"
    else
        printf "\b%bfailed%b\n" "${COLOR_RED}" "${COLOR_NONE}"
    fi

    if [ $resultCode != 0 ] && [ -n "$result" ]; then
        printf "%s\n" "$result"
    fi

    return $resultCode
}

############################################################
# Help                                                     #
############################################################
Help() {
   echo "Attempts to change the standard package name of the project to the name specified as the first parameter"
   echo
   echo "Syntax: changePackagename.sh [-s|h] new.packagename.app"
   echo "options:"
   echo "s     Skip deletion prompt after successful rename run."
   echo "h     Print this Help."
   echo
}

############################################################
# Process the input options. Add options as needed.        #
############################################################
# Get the options
while getopts ":sh" option; do
   case $option in
      s)
        SKIP_DELETION=true
        shift
        ;;
      h) # display Help
         Help
         exit;;
      *)
        shift
        ;;
   esac
done

if [ $# -ne 1 ] || [ -z "$1" ]; then
    printf "%bNo new package name given\n" "${PREFIX_LOG_ERROR}"
    printf "Usage: %s new.package.name\n" "$0"
    exit 1
fi

PACKAGE=$1

if [[ $1 =~ $REGEX_PACKAGE_NAME ]]; then
    subpath_package_new=kotlin/${PACKAGE//[.]/$SEPARATOR_FOLDER}
    src_flavors=($(find $PATH_FOLDER_SRC -mindepth 1 -maxdepth 1 -type d))

    for flavor in ${src_flavors[*]}
    do
        flavor_parts=(${flavor//\// })
        flavor_name=${flavor_parts[$((${#flavor_parts[*]}-1))]}
        printf "%bFlavor %s\n" "${PREFIX_LOG_CHAPTER}" "$flavor_name"

        path_package_old="$flavor${SEPARATOR_FOLDER}$SUBPATH_PACKAGE_OLD"
        path_package_new="$flavor${SEPARATOR_FOLDER}$subpath_package_new"

        step "Moving" "$path_package_old -> $path_package_new" "mkdir -p $path_package_new && mv $path_package_old/* $path_package_new"

        step "Fixing" "Package Declarations" "find $path_package_new -type f -exec sed $OPTIONS_SED \"s/package $PACKAGE_OLD/package $PACKAGE/g\" {} +"

        step "Fixing" "Imports" "find $path_package_new -type f -exec sed $OPTIONS_SED \"s/import $PACKAGE_OLD/import $PACKAGE/g\" {} +"

        if [ -f "$flavor${SEPARATOR_FOLDER}AndroidManifest.xml" ]; then
            step "Fixing" "App Package ID" "sed $OPTIONS_SED 's/package=\"$PACKAGE_OLD/package=\"$PACKAGE/g' $flavor${SEPARATOR_FOLDER}AndroidManifest.xml"
        else
            printf "%bFixing%b - Package ID: %bskipped%b\n" "${COLOR_HIGHLIGHT}" "${COLOR_NONE}" "${COLOR_YELLOW}" "${COLOR_NONE}"
        fi

        step "Fixing" "OpenAPI Package ID" "sed $OPTIONS_SED 's/packageName.set(\"$PACKAGE_OLD/packageName.set(\"$PACKAGE/g' api-backend${SEPARATOR_FOLDER}build.gradle.kts"

        nav_graphs=($(find $flavor${SEPARATOR_FOLDER}res${SEPARATOR_FOLDER}navigation -mindepth 1 -maxdepth 1 -type f))
        for graph in ${nav_graphs[*]}
        do
          step "Fixing" "Navigation Graph $graph" "sed $OPTIONS_SED 's/name=\"$PACKAGE_OLD/name=\"$PACKAGE/g' $graph"
        done

        path_folder_delete=$SUBPATH_PACKAGE_OLD
        while [[ $path_folder_delete == *$SEPARATOR_FOLDER* ]]
        do
            step "Cleaning" "Package Folder $flavor${SEPARATOR_FOLDER}$path_folder_delete" "rm -d $flavor${SEPARATOR_FOLDER}$path_folder_delete"
            path_folder_delete=${path_folder_delete%$SEPARATOR_FOLDER*}
        done
    done

    printf "%bGeneral\n" "${PREFIX_LOG_CHAPTER}"
    step "Cleanup" "Reformat Code according to Code Style" "./gradlew ktlintFormat --rerun-tasks"

    RESULT_CODE_LAST_METHOD_CALL=$?
    if [ $RESULT_CODE_LAST_METHOD_CALL == 0 ]; then
      if [[ $SKIP_DELETION == false ]] ; then
        echo "Rename finished successfully. Would you like to remove this script?"
        select answer in "Yes, remove script" "No, keep script"; do
        case $answer in
            "Yes, remove script"  ) git rm -f "$PATH_OF_SCRIPT"; break;;
            "No, keep script"     ) exit 0;;
        esac
        done
      fi
    fi
else
    printf "%bGiven \"%s\" does not appear to be a valid package name\n" "${PREFIX_LOG_ERROR}" "$1"
    exit 2
fi
