# Multilingual-qanus

An integration of qanus framework with freeling api.

## Setup

1. Clone the project: `git clone git@github.com:julian3833/multilingual-qanus.git`
2. Download [qanus v2.9][1] and extract the files in `dist/lib/` into `project-root/dist/lib/`
3. Follow the step in **Freeling Installation** 
4. Download [this zip][2] and extract the jars in `project-root/dist/lib/`
(this step is quiet dirty and is going to be refactored)
5. Remove `lucene-core-2.4.0.jar` from `project-root/dist/lib`
6. Follow the steps described in **Wikipedia Dumps**
7. Configure the constants in `ar.uba.dc.galli.qa.ml.util.Configuration.WIKIDIR` and `INDEXDIR` 
7. Run `ant build-jar` in `project-root`. This will create the file `dist/ml.jar`
8. `cd dist`
9. Test if installation was ok:
```
 java -Xmx1g -classpath ml.jar ar.uba.dc.galli.qa.ml.ibp.Controller --wiki [simple-06|simple-13|es-06|en-06|pt-07]
 java -Xmx1g -classpath ml.jar ar.uba.dc.galli.qa.ml.qp.Controller ??
 java -Xmx1g -classpath ml.jar ar.uba.dc.galli.qa.ml.ar.Controller --run [es-06|pt-07]
```

#Other
1. Obtain and configure de question files in Config.java


##Freeling Installation

1. Add the .so to java.library.path. This is the easy way: `export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:project-root/`
6. This is the correct way:  `sudo cp libfreeling_javaAPI.so /usr/lib/`

##Wikipedia Dumps

##Usage




 [1]: http://wing.comp.nus.edu.sg/~junping/qanus/QANUSv29112012.zip "Qanus v 2.9"
 [2]: http://remotehost.no-ip.org/resources.zip "All jar dependencies in a zip"

