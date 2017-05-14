package de.uni_potsdam.hpi.metanome_test_runner;

import de.uni_potsdam.hpi.metanome_test_runner.config.Config;
import de.uni_potsdam.hpi.metanome_test_runner.mocks.MetanomeMock;

public class MainTestAlgo {

  public static void main(String[] args) {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC11);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC011);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC101);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC111);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCALL);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCNO);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCONE);
    MetanomeMock.execute(conf);
    conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCTWO);
    MetanomeMock.execute(conf);
  }
}
