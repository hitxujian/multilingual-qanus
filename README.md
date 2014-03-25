# Multilingual-qanus

An integration of qanus framework with freeling api.

## Setup

1. Clone the project: `git clone git@github.com:julian3833/multilingual-qanus.git`
2. Download [qanus v2.9][1] and extract the files in `dist/lib/` into `project-root/dist/lib/`
3. Follow the step in **Freeling Installation** 
4. Download [this][2] and [this][3] files and place them in `project-root/dist/lib/`
(this step is quiet dirty and is going to be refactored)
5. Run `ant build-jar` in `project-root`. This will create the file `dist/ml.jar`
6. `cd dist`
8. Test if installation was ok:
```
java -Xmx1g -classpath ml.jar ar.uba.dc.galli.qa.ml.ibp.Controller --src ../demo-data/data --tgt ../temp
```



##Freeling Installation

##Usage




 [1]: http://wing.comp.nus.edu.sg/~junping/qanus/QANUSv29112012.zip "Qanus v 2.9"
 [2]: http://remotehost.no-ip.org/freeling.jar "Freeling Java API"
 [3]: http://remotehost.no-ip.org/commons-lang3-3.1.jar "Apache Commons Lang3"
