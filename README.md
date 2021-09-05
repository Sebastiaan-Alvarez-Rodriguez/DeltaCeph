# Delta-Ceph
This project aims to interconnect Delta Lake with Ceph.
The interconnection is particularly aimed at offloading operations from compute- to storage systems,
when using Delta Lake together with Spark to compute data.

In a layered list, you will find the following technologies, starting from a user point-of-view, going deeper:
 1. Apache Spark
 2. Delta Lake
 3. Spark (Delta Lake uses Spark internally)
 4. Arrow-Spark
 5. Apache Arrow (C++ core)
 6. Skyhook
 7. Ceph


## Compiling this project
This project uses [this](https://github.com/Sebastiaan-Alvarez-Rodriguez/delta) custom flavor 
of Delta Lake, that allows us to capture and forward data reading/writing to Arrow-Spark.

 1. You must compile custom Delta Lake.
    For build instructions on how to do this, refer to [the repository](https://github.com/Sebastiaan-Alvarez-Rodriguez/delta).
 2. Build Arrow-Spark, and **make sure** you have it in your local `maven` repository.
    For build instructions on how to do this, refer to [the repository](https://github.com/Sebastiaan-Alvarez-Rodriguez/arrow-spark).
 3. Once you have finished step 1, i.e, you have the custom Delta Lake as a `JAR` file at `some/path/custom_delta.jar`,
execute the following in the root folder of this project:
```bash
mkdir depjars
cp some/path/custom_delta.jar depjars/
```
 4. Finally, choose one of the following commands to build:
    1. `./gradlew shadowJar` builds the full `JAR` file, with all dependencies included.
    2. `./gradlew lightJar` builds a `JAR` file, skipping a bunch of dependencies that Apache Spark already has.
       This is a useful build target when planning to deploy on a Spark cluster.
    3. `./gradlew essentialJar` builds a `JAR` file, skipping all but the absolute essentials.
       This is a useful build target when planning to deploy on a Spark production cluster.
 
After compilation, you will find the output `JAR` in:
```
app/build/libs/app-all.jar       (if shadowJar-compiled)
app/build/libs/app-light.jar     (if lightJar-compiled)
app/build/libs/app-essential.jar (if essentialJar-compiled)
```