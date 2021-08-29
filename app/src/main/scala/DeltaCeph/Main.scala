package DeltaCeph

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, SparkSession}
import org.arrowspark.spark.sql.write.ArrowWriteExtension

import java.nio.file.{Files, Paths}

object Main {

    private def getSession(name: String, testing: Option[String]) = testing match {
        case Some(value) => SparkSession.builder.appName(name)
            .config("spark.master", s"local[$value]")
            .config("spark.arrowspark.pushdown.filters", "true")
            .config("spark.arrowspark.ceph.userados", "false")
            .config("spark.sql.extensions", classOf[ArrowWriteExtension].getCanonicalName+','+"io.delta.sql.DeltaSparkSessionExtension")
            .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
            .getOrCreate()
        case None => SparkSession.builder.appName(name).getOrCreate()
    }

    /** Forcefully triggers execution for Datasets (and for Dataframes too, as a `DataFrame` simply is another term for `Dataset[Row]`) */
    //noinspection ScalaUnusedSymbol
    def triggerExecution(ds: Dataset[_]): Unit = {
        val plan = ds.queryExecution.executedPlan.execute()
        val x: Long = plan.count()
    }

    /**
     * Builds a simple sample Delta Lake table, by
     *  1. Reading a regular parquet file.
     *  2. Storing using the delta format.
     * @param session Session to use.
     * @param storePath Path to store Delta table.
     */
    def buildSampleSet(session: SparkSession, storePath: String): Unit = {
        val srcPath = "sample/sources/128MB.parquet"

        val df = session.read.format("parquet").load(srcPath)
        df.write.format("delta").save(storePath)
    }

    /**
     * Performs a basic read operation.
     * @param session Session to use.
     * @param storePath Path containing Delta table.
     */
    def checkRead(session: SparkSession, storePath: String): Unit = {
        val df = session.read.format("delta").load(storePath)
        val colnames = df.schema.names

        val untriggeredDF = df.select(colnames.map(name => col(name)):_*)
        print(untriggeredDF)
        triggerExecution(untriggeredDF)
    }

    def main(args: Array[String]): Unit = {
        val deltaStorePath = Paths.get("sample", "delta", "try1")

        println("Starting Delta...")
        val session = getSession("test", Option("1")) // start Spark session, 1 core, local execution.
        if (Files.notExists(deltaStorePath) || Files.notExists(deltaStorePath.resolve("_delta_log").resolve("00000000000000000000.json")))
            buildSampleSet(session, deltaStorePath.toString)
        checkRead(session, deltaStorePath.toString)

        session.close()
    }
}
