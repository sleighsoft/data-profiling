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

public class SuperUCC extends SuperUCCAlgorithm // Separating the algorithm implementation and the Metanome interface implementation is good practice
    implements UniqueColumnCombinationsAlgorithm, // Defines the type of the algorithm, i.e., the result type, for instance, FunctionalDependencyAlgorithm or InclusionDependencyAlgorithm; implementing multiple types is possible
    RelationalInputParameterAlgorithm {

  public enum Identifier {
    INPUT_GENERATOR, SOME_STRING_PARAMETER, SOME_INTEGER_PARAMETER, SOME_BOOLEAN_PARAMETER
  }

  ;

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
    //conf.add(new ConfigurationRequirementRelationalInput(MyIndDetector.Identifier.INPUT_GENERATOR.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES)); // An algorithm can ask for more than one input; this is typical for IND detection algorithms

    ConfigurationRequirementString stringParameter = new ConfigurationRequirementString(
        SuperUCC.Identifier.SOME_STRING_PARAMETER.name());
    String[] defaultStringParameter = new String[1];
    defaultStringParameter[0] = "default value";
    stringParameter.setDefaultValues(defaultStringParameter);
    stringParameter.setRequired(true);
    conf.add(stringParameter);

    ConfigurationRequirementInteger integerParameter = new ConfigurationRequirementInteger(
        SuperUCC.Identifier.SOME_INTEGER_PARAMETER.name());
    Integer[] defaultIntegerParameter = new Integer[1];
    defaultIntegerParameter[0] = Integer.valueOf(42);
    integerParameter.setDefaultValues(defaultIntegerParameter);
    integerParameter.setRequired(true);
    conf.add(integerParameter);

    ConfigurationRequirementBoolean booleanParameter = new ConfigurationRequirementBoolean(
        SuperUCC.Identifier.SOME_BOOLEAN_PARAMETER.name());
    Boolean[] defaultBooleanParameter = new Boolean[1];
    defaultBooleanParameter[0] = Boolean.valueOf(true);
    booleanParameter.setDefaultValues(defaultBooleanParameter);
    booleanParameter.setRequired(true);
    conf.add(booleanParameter);

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
