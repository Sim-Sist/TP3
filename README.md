
# TP3
## Setup
```bash
cd ./src
chmod +x exec.sh
```
## Execution
```bash
cd ./src
./exec.sh
```
## Output files' format
All the output files formats are defined on `src/utils/formats.txt`

To define a format for a simulation's output file it is necesary to add the following specifications:

- **Name:** The name of the format, as will appear on the output folder
- **Header:** Usually contains important or general (always unique) information about the system
- **Body:** The main information about the system. The structure here defined is meant to appear repeated, since it represents values about the elements of the system

On the actual output file the header and the body are always separated by an empty line. If the file format has no header, then the output file should have a blank line as its first line.

On the body, if one file has many values separated by spaces, on the `formats.txt` file, the name of those values should appear separated by commas

Also, on the `formats.txt` file, all the different file formats are separated by empty lines. If more clarification is needed, then one can add comments with the '#' token

An example of a format definition would be as follows:
```txt
# formats.txt

dynamic-info
Header:
    amount of particles
    space size 
    noise level # this parameter can have a max value of 5
Body:
    index of particle
    x coordinate, y coordinate
    radius, color, name

```
