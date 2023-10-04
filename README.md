# temp

[![CI/CD](https://github.com/Emut/temp/actions/workflows/maven.yml/badge.svg?branch=feature%2Fesg-engine-dependency-removal)](https://github.com/Emut/temp/actions/workflows/maven.yml)
![Coverage](.github/badges/jacoco.svg)
![Coverage](.github/badges/branches.svg)

## Usage

Compile and package with `mvn package`

Run the tool with:

```bash
java -jar SequenceDiagram-to-ESG-Transformer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

```
Usage: SequenceDiagram2ESG [-hV] [-d=<esgVisualOutputDirectory>]
                           [-f=<outputFile>] [-o=<esgOutputFile>] <inputFile>
Convert PlantUML Sequence Diagram to ESG.
      <inputFile>   Sequence Diagram file.
  -h, --help        Show this help message and exit.
  -V, --version     Print version information and exit.
  -d, --visual-dir=<esgVisualOutputDirectory>
                    Output directory for ESG visualizations.
  -f, --export-frame=<outputFile>
                    Frame json output filename.
  -o, --esg-output=<esgOutputFile>
                    ESG json output filename (ESG.json if not given).

```
## Pro-tip

CLI tool works on a single file. Use `find` and `xargs` to quickly iterate over many examples:
```bash
find docs/examples/Tschonti_503-service-unavailable/ -name '*.puml' | xargs -I{} java -jar target/SequenceDiagram-to-ESG-Transformer-1.0-SNAPSHOT-jar-with-dependencies.jar {} -f{}_frame.json
```

To clone from github, batch-generate test sequences and collect metadata:
```bash
sh ./docs/fetchAndGenerateTS.sh <git repo URL>
find . -name 'sd2esgresults.txt' | 
  xargs -I{} awk '{ printf "%s ", $0 } END{print ""}' {} |
  awk '{print $1, $3, $8, $9, $11, $12, $14, $15}' |
  sort | uniq -u > summary.txt
```
