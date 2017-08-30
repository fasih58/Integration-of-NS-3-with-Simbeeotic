//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.03.07 at 01:36:48 PM PKT 
//


package harvard.robobees.simbeeotic.configuration.scenario;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the harvard.robobees.simbeeotic.configuration.scenario package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Scenario_QNAME = new QName("http://harvard/robobees/simbeeotic/configuration/scenario", "scenario");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: harvard.robobees.simbeeotic.configuration.scenario
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Variable }
     * 
     */
    public Variable createVariable() {
        return new Variable();
    }

    /**
     * Create an instance of {@link CustomConfig }
     * 
     */
    public CustomConfig createCustomConfig() {
        return new CustomConfig();
    }

    /**
     * Create an instance of {@link UniformRandomLoopingVariable }
     * 
     */
    public UniformRandomLoopingVariable createUniformRandomLoopingVariable() {
        return new UniformRandomLoopingVariable();
    }

    /**
     * Create an instance of {@link Scenario }
     * 
     */
    public Scenario createScenario() {
        return new Scenario();
    }

    /**
     * Create an instance of {@link Models }
     * 
     */
    public Models createModels() {
        return new Models();
    }

    /**
     * Create an instance of {@link ConfigProps }
     * 
     */
    public ConfigProps createConfigProps() {
        return new ConfigProps();
    }

    /**
     * Create an instance of {@link Vector }
     * 
     */
    public Vector createVector() {
        return new Vector();
    }

    /**
     * Create an instance of {@link ConstantLoopingVariable }
     * 
     */
    public ConstantLoopingVariable createConstantLoopingVariable() {
        return new ConstantLoopingVariable();
    }

    /**
     * Create an instance of {@link SensorConfig }
     * 
     */
    public SensorConfig createSensorConfig() {
        return new SensorConfig();
    }

    /**
     * Create an instance of {@link Simulation }
     * 
     */
    public Simulation createSimulation() {
        return new Simulation();
    }

    /**
     * Create an instance of {@link RadioConfig }
     * 
     */
    public RadioConfig createRadioConfig() {
        return new RadioConfig();
    }

    /**
     * Create an instance of {@link Looping }
     * 
     */
    public Looping createLooping() {
        return new Looping();
    }

    /**
     * Create an instance of {@link ModelConfig }
     * 
     */
    public ModelConfig createModelConfig() {
        return new ModelConfig();
    }

    /**
     * Create an instance of {@link ConfigProps.Prop }
     * 
     */
    public ConfigProps.Prop createConfigPropsProp() {
        return new ConfigProps.Prop();
    }

    /**
     * Create an instance of {@link NSConfig }
     * 
     */
    public NSConfig createNSConfig() {
        return new NSConfig();
    }

    /**
     * Create an instance of {@link EachLoopingVariable }
     * 
     */
    public EachLoopingVariable createEachLoopingVariable() {
        return new EachLoopingVariable();
    }

    /**
     * Create an instance of {@link Components }
     * 
     */
    public Components createComponents() {
        return new Components();
    }

    /**
     * Create an instance of {@link NormalRandomLoopingVariable }
     * 
     */
    public NormalRandomLoopingVariable createNormalRandomLoopingVariable() {
        return new NormalRandomLoopingVariable();
    }

    /**
     * Create an instance of {@link VariableMasterSeed }
     * 
     */
    public VariableMasterSeed createVariableMasterSeed() {
        return new VariableMasterSeed();
    }

    /**
     * Create an instance of {@link ConstantMasterSeed }
     * 
     */
    public ConstantMasterSeed createConstantMasterSeed() {
        return new ConstantMasterSeed();
    }

    /**
     * Create an instance of {@link MasterSeed }
     * 
     */
    public MasterSeed createMasterSeed() {
        return new MasterSeed();
    }

    /**
     * Create an instance of {@link ForLoopingVariable }
     * 
     */
    public ForLoopingVariable createForLoopingVariable() {
        return new ForLoopingVariable();
    }

    /**
     * Create an instance of {@link CustomClass }
     * 
     */
    public CustomClass createCustomClass() {
        return new CustomClass();
    }

    /**
     * Create an instance of {@link Variables }
     * 
     */
    public Variables createVariables() {
        return new Variables();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Scenario }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://harvard/robobees/simbeeotic/configuration/scenario", name = "scenario")
    public JAXBElement<Scenario> createScenario(Scenario value) {
        return new JAXBElement<Scenario>(_Scenario_QNAME, Scenario.class, null, value);
    }

}
