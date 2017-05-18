package de.metanome.algorithms.superucc;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.*;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

public class SuperUCC extends SuperUCCAlgorithm
    implements UniqueColumnCombinationsAlgorithm, // Defines the type of the algorithm, i.e., the result type, for instance, FunctionalDependencyAlgorithm or InclusionDependencyAlgorithm; implementing multiple types is possible
    RelationalInputParameterAlgorithm {

  public enum Identifier {
    INPUT_GENERATOR, SOME_STRING_PARAMETER, SOME_INTEGER_PARAMETER, SOME_BOOLEAN_PARAMETER
  }

  @Override
  public String getAuthors() {
    return "Julian Niedermeier, Florian Wagner, Sebastian Ernst"; // A string listing the author(s) of this algorithm
  }

  @Override
  public String getDescription() {
    return "SuperUCC - An Algorithm to detect Unique Column Combinations"; // A string briefly describing what this algorithm does
  }

  //
  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() { // Tells Metanome which and how many parameters the algorithm needs
    ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
    conf.add(new ConfigurationRequirementRelationalInput(SuperUCC.Identifier.INPUT_GENERATOR.name()));
    return conf;
  }

  @Override
  public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (!SuperUCC.Identifier.INPUT_GENERATOR.name().equals(identifier))
      this.handleUnknownConfiguration(identifier, values);
    this.inputGenerator = values[0];
  }

  @Override
  public void setResultReceiver(UniqueColumnCombinationResultReceiver uniqueColumnCombinationResultReceiver) {
    this.resultReceiver = uniqueColumnCombinationResultReceiver;
  } // Defines the input type of the algorithm; relational input is any relational input from files or databases; more specific input specifications are possible

  @Override
  public void execute() throws AlgorithmExecutionException {
    super.execute();
  }

  private void handleUnknownConfiguration(String identifier, Object[] values) throws AlgorithmConfigurationException {
    throw new AlgorithmConfigurationException(
        "Unknown configuration: " + identifier + " -> [" + concat(values, ",") + "]");
  }

  private static String concat(Object[] objects, String separator) {
    if (objects == null)
      return "";

    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < objects.length; i++) {
      buffer.append(objects[i].toString());
      if ((i + 1) < objects.length)
        buffer.append(separator);
    }
    return buffer.toString();
  }
}
