#!/bin/bash

main_file="analyzer"

script_dir=`dirname $0`
py_dir=${script_dir}/main/python
venv_dir=${py_dir}/venv

function update {
    pip install --requirement ${venv_dir}/requirements.txt
}

source ${venv_dir}/bin/activate


if [ "$1" == "install" ]; then
    module_name=$2
    req_file=${venv_dir}/requirements.txt    
    if grep -q "${module_name}" "$req_file"; then
       echo "already installed"
       exit
    else
        echo -e "${module_name}" | tr -d '\n' >> ${req_file}
        package_version=`pip freeze | grep ${module_name} | egrep -o "==.*"`
    fi
    update
    echo -e "${package_version}" >> ${req_file}   
    exit
fi

# update dependencies on request
if [ "$1" == "update" ]; then
    update;
    echo -e "\n\n"
fi

python ${py_dir}/${main_file}.py

