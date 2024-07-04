#!/bin/bash

# Cambia al directorio broker-service
cd ..
MVN_ARG_LINE=()

for arg in "$@"
do
    case "$arg" in
        *)
            MVN_ARG_LINE+=("$arg")
        ;;
    esac
done

startDateTime=`date +%s`

# Verifica que los argumentos de Maven no estén vacíos
if [ "$MVN_ARG_LINE" != "" ] ; then
    mvnBin="mvn"
    if [ -a $M3_HOME/bin/mvn ] ; then
       mvnBin="$M3_HOME/bin/mvn"
    fi
    echo
    echo "Running maven build on available projects (using Maven binary '$mvnBin')"

    "$mvnBin" -v
    echo
    projects=( "fkbroker-service" )

    # Itera sobre cada tipo de proyecto definido en la variable projects
    for suffix in "${projects[@]}"; do

        for repository in $suffix; do
        echo
            if [ -d "$repository" ]; then
                echo "==============================================================================="
                echo "$repository"
                echo "==============================================================================="

                cd $repository

                # Ejecuta Maven con los argumentos proporcionados
                "$mvnBin" "${MVN_ARG_LINE[@]}"
                returnCode=$?

                # Si Maven retorna un código de error, termina el script
                if [ $returnCode != 0 ] ; then
                    exit $returnCode
                fi

                cd ..
                fi

        done;
    done;
    endDateTime=`date +%s`
    spentSeconds=`expr $endDateTime - $endDateTime`
    echo
    echo "Total build time: ${spentSeconds}s"

else
    echo "No Maven arguments skipping maven build"
fi

# Lanza la aplicación localmente
echo "Launching the application locally..."
pattern="broker-service"
files=( $pattern )
cd ${files[0]}
executable="$(ls *target/*.jar | tail -n1)"
java -jar "$executable"
