package de.uni_potsdam.hpi.algo;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.metanome.backend.result_receiver.ResultCache;
import de.uni_potsdam.hpi.metanome_test_runner.config.Config;
import de.uni_potsdam.hpi.metanome_test_runner.mocks.MetanomeMock;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AlgorithmTest {

  @Test
  public void test11UCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC11);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    UniqueColumnCombination uccs = (UniqueColumnCombination) result.fetchNewResults().get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(2, columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
    assertEquals("C2", columns[1].getColumnIdentifier());
  }

  @Test
  public void test011UCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC011);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    UniqueColumnCombination uccs = (UniqueColumnCombination) result.fetchNewResults().get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(2,columns.length);
    assertEquals("C2", columns[0].getColumnIdentifier());
    assertEquals("C3", columns[1].getColumnIdentifier());
  }

  @Test
  public void test101UCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC101);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    UniqueColumnCombination uccs = (UniqueColumnCombination) result.fetchNewResults().get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(2, columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
    assertEquals("C3", columns[1].getColumnIdentifier());
  }

  @Test
  public void test111UCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCC111);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    UniqueColumnCombination uccs = (UniqueColumnCombination) result.fetchNewResults().get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(3,columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
    assertEquals("C2", columns[1].getColumnIdentifier());
    assertEquals("C3", columns[2].getColumnIdentifier());
  }

  @Test
  public void testALLUCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCALL);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    List<Result> uccs = result.fetchNewResults();
    String c1 = ((UniqueColumnCombination) uccs.get(0)).getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0].getColumnIdentifier();
    String c2 = ((UniqueColumnCombination) uccs.get(1)).getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0].getColumnIdentifier();
    String c3 = ((UniqueColumnCombination) uccs.get(2)).getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0].getColumnIdentifier();
    assertEquals(3, uccs.size());
    assertEquals("C1", c1);
    assertEquals("C2", c2);
    assertEquals("C3", c3);
  }

  @Test
  public void testNOUCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCNO);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    assertEquals(0,result.fetchNewResults().size());
  }

  @Test
  public void testONEUCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCONE);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    List<Result> results = result.fetchNewResults();
    assertEquals(1,results.size());
    UniqueColumnCombination uccs = (UniqueColumnCombination) results.get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(1,columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
  }

  @Test
  public void testTWOUCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCTWO);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    List<Result> results = result.fetchNewResults();
    assertEquals(2,results.size());
    UniqueColumnCombination uccs = (UniqueColumnCombination) results.get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(1, columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
    uccs = (UniqueColumnCombination) results.get(1);
    columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(1,columns.length);
    assertEquals("C2", columns[0].getColumnIdentifier());
  }

  @Test
  public void testOneColumnOneUCC() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.UCCONECOLUMNONE);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    List<Result> results = result.fetchNewResults();
    assertEquals(1,results.size());
    UniqueColumnCombination uccs = (UniqueColumnCombination) results.get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(1, columns.length);
    assertEquals("C1", columns[0].getColumnIdentifier());
  }

  @Test
  public void testPruneTooMuch() {
    Config conf = new Config(Config.Algorithm.SuperUCC, Config.Dataset.PRUNETOOMUCH);
    ResultCache result = MetanomeMock.executeWithResult(conf);
    List<Result> results = result.fetchNewResults();
    assertEquals(1, results.size());
    UniqueColumnCombination uccs = (UniqueColumnCombination) results.get(0);
    ColumnIdentifier[] columns = uccs.getColumnCombination().getColumnIdentifiers().toArray(new ColumnIdentifier[]{});
    assertEquals(2,columns.length);
    assertEquals("C2", columns[0].getColumnIdentifier());
    assertEquals("C3", columns[0].getColumnIdentifier());
  }
}
