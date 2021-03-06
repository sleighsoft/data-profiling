package de.uni_potsdam.hpi.metanome_test_runner.config;

import java.io.File;

public class Config {

  public enum Algorithm {
    SuperUCC
  }

  public enum Dataset {
    PLANETS, SYMBOLS, SCIENCE, SATELLITES, GAME, ASTRONOMICAL, ABALONE, ADULT, BALANCE, BREAST, BRIDGES, CHESS, ECHODIAGRAM, FLIGHT, HEPATITIS, HORSE, IRIS, LETTER, NURSERY, PETS, NCVOTER_1K, UNIPROD_1K, UCC11, UCC011, UCC101, UCC111, UCCALL, UCCNO, UCCTWO, UCCONE, UCCONECOLUMNONE, PRUNETOOMUCH, COLUMNS20, NULLVALUES
  }

  public Config.Algorithm algorithm;
  public Config.Dataset dataset;

  public String someStringParameter = "MyStringParamaterValue";
  public Integer someIntegerParameter = Integer.valueOf(42);
  public Boolean someBooleanParameter = Boolean.valueOf(true);

  public String inputDatasetName;
  public String inputFolderPath = "data" + File.separator;
  public String inputFileEnding = ".csv";
  public String inputFileNullString = "";
  public char inputFileSeparator;
  public char inputFileQuotechar = '\"';
  public char inputFileEscape = '\\';
  public int inputFileSkipLines = 0;
  public boolean inputFileStrictQuotes = false;
  public boolean inputFileIgnoreLeadingWhiteSpace = true;
  public boolean inputFileHasHeader;
  public boolean inputFileSkipDifferingLines = true; // Skip lines that differ from the dataset's schema

  public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator;

  public String statisticsFileName = "statistics.txt";
  public String resultFileName = "results.txt";

  public boolean writeResults = true;

  public static Config create(String[] args) {
    if (args.length == 0)
      return new Config();

    Config.Algorithm algorithm = null;
    String algorithmArg = args[0].toLowerCase();
    for (Config.Algorithm possibleAlgorithm : Config.Algorithm.values())
      if (possibleAlgorithm.name().toLowerCase().equals(algorithmArg))
        algorithm = possibleAlgorithm;

    Config.Dataset dataset = null;
    String datasetArg = args[1].toLowerCase();
    for (Config.Dataset possibleDataset : Config.Dataset.values())
      if (possibleDataset.name().toLowerCase().equals(datasetArg))
        dataset = possibleDataset;

    if ((algorithm == null) || (dataset == null))
      wrongArguments();

    return new Config(algorithm, dataset);
  }

  private static void wrongArguments() {
    StringBuilder message = new StringBuilder();
    message.append("\r\nArguments not supported!");
    message.append("\r\nProvide correct values: <algorithm> <dataset>");
    throw new RuntimeException(message.toString());
  }

  public Config() {
    this(Config.Algorithm.SuperUCC, Config.Dataset.SYMBOLS);
  }

  public Config(Config.Algorithm algorithm, Config.Dataset dataset) {
    this.algorithm = algorithm;
    this.setDataset(dataset);
  }

  @Override
  public String toString() {
    return "Config:\r\n\t" +
        "algorithm: " + this.algorithm.name() + "\r\n\t" +
        "dataset: " + this.inputDatasetName + this.inputFileEnding;
  }

  private void setDataset(Config.Dataset dataset) {
    this.dataset = dataset;
    switch (dataset) {
      case PLANETS:
        this.inputDatasetName = "WDC_planets";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case SYMBOLS:
        this.inputDatasetName = "WDC_symbols";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case SCIENCE:
        this.inputDatasetName = "WDC_science";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case SATELLITES:
        this.inputDatasetName = "WDC_satellites";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case GAME:
        this.inputDatasetName = "WDC_game";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case ASTRONOMICAL:
        this.inputDatasetName = "WDC_astronomical";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case ABALONE:
        this.inputDatasetName = "abalone";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case ADULT:
        this.inputDatasetName = "adult";
        this.inputFileSeparator = ';';
        this.inputFileHasHeader = false;
        break;
      case BALANCE:
        this.inputDatasetName = "balance-scale";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case BREAST:
        this.inputDatasetName = "breast-cancer-wisconsin";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case BRIDGES:
        this.inputDatasetName = "bridges";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case CHESS:
        this.inputDatasetName = "chess";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case ECHODIAGRAM:
        this.inputDatasetName = "echocardiogram";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case FLIGHT:
        this.inputDatasetName = "flight_1k";
        this.inputFileSeparator = ';';
        this.inputFileHasHeader = true;
        break;
      case HEPATITIS:
        this.inputDatasetName = "hepatitis";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case HORSE:
        this.inputDatasetName = "horse";
        this.inputFileSeparator = ';';
        this.inputFileHasHeader = false;
        break;
      case IRIS:
        this.inputDatasetName = "iris";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case LETTER:
        this.inputDatasetName = "letter";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case NURSERY:
        this.inputDatasetName = "nursery";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = false;
        break;
      case PETS:
        this.inputDatasetName = "pets";
        this.inputFileSeparator = ';';
        this.inputFileHasHeader = true;
        break;
      case NCVOTER_1K:
        this.inputDatasetName = "ncvoter_1001r_19c";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case UNIPROD_1K:
        this.inputDatasetName = "uniprot_1001r_223c";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        break;
      case UCC11:
        this.inputDatasetName = "11_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCC011:
        this.inputDatasetName = "011_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCC101:
        this.inputDatasetName = "101_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCC111:
        this.inputDatasetName = "111_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCCALL:
        this.inputDatasetName = "all_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCCNO:
        this.inputDatasetName = "no_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCCONE:
        this.inputDatasetName = "one_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCCTWO:
        this.inputDatasetName = "two_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case UCCONECOLUMNONE:
        this.inputDatasetName = "1_column_1_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case PRUNETOOMUCH:
        this.inputDatasetName = "prune_too_much";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case COLUMNS20:
        this.inputDatasetName = "20_columns_ucc";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
      case NULLVALUES:
        this.inputDatasetName = "null_values";
        this.inputFileSeparator = ',';
        this.inputFileHasHeader = true;
        this.inputFolderPath = "data" + File.separator + "test" + File.separator;
        break;
    }
  }
}
