package org.simbrain.custom.other_stuff;

import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.simulation.Vehicle;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;

// TODO: Rename!

/**
 * Sample simulation to use as a model for your own simulations.
 */
public class TestSim {

    final Simulation sim;

    /**
     * @param desktop
     */
    public TestSim(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
    }

    /**
     * Run the simulation!
     */
    public void run() {
        
        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Build a network
        NetBuilder net = sim.addNetwork(10, 10, 450, 450, "My first network");
        // nb1.addNeurons(0, 0, 20, "horizontal line", "LinearRule");
        // nb1.addNeurons(0, 89, 20, "vertical line", "LinearRule");
        // nb1.addNeurons(89, 89, 49, "grid", "LinearRule");
        // NeuronGroup inputs = net1.addNeuronGroup(0, 300, 6, "horizontal
        // line",
        // "DecayRule");
        // inputs.setLabel("Inputs");
        // NeuronGroup outputs = net1.addNeuronGroup(0, 0, 3, "horizontal line",
        // "DecayRule");
        // outputs.setUpperBound(10);
        // outputs.setLabel("Outputs");
        // // nb1.connectAllToAll(inputs, outputs);
        // SynapseGroup in_out = net1.addSynapseGroup(inputs, outputs);
        // in_out.setExcitatoryRatio(.5);
        // in_out.randomizeConnectionWeights(); // TODO Not working?

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(460, 10, 450, 450,
                "My first world");
        world.getWorld().setObjectsBlockMovement(false);
        RotatingEntity mouse = world.addAgent(20, 20, "Mouse");
        RotatingEntity mouse2 = world.addAgent(200, 200, "Mouse");
        RotatingEntity mouse3 = world.addAgent(400, 200, "Mouse");

        OdorWorldEntity cheese = world.addEntity(150, 150, "Swiss.gif",
                new double[] { 0, 1, 0, 0 });
        cheese.getSmellSource().setDispersion(400);

        // Coupling agent to network
        // sim.couple(mouse, inputs); // Agent sensors to neurons
        // sim.couple(outputs, mouse); // Neurons to movement effectors

        // Add vehicles
        Vehicle vehicleBuilder = new Vehicle(sim, net, world);
        NeuronGroup pursuer1 = vehicleBuilder.addPursuer(0, 400, mouse, 2);
        pursuer1.setLabel("Pursuer 1");
        NeuronGroup pursuer2 = vehicleBuilder.addPursuer(240, 400, mouse2, 2);
        pursuer2.setLabel("Pursuer 2");
        NeuronGroup avoider1 = vehicleBuilder.addAvoider(480, 400, mouse3, 2);
        avoider1.setLabel("Avoider 1");
        
//        // Add input-output network
//        NeuronGroup inputNodes = net.addNeuronGroup(-350, 500, 5);
//        inputNodes.setLabel("Inputs");
//        inputNodes.setClamped(true);
//        NeuronGroup outputNodes = net.addNeuronGroup(-350, 200, 5);
//        outputNodes.setLabel("Outputs");
//        net.connectAllToAll(inputNodes, outputNodes);
//        
//        // Couple i/o to mouse
//        sim.couple(mouse, inputNodes);
    }

}