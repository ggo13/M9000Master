package com.usi.m9000.test;

import java.util.HashMap;
import java.util.Map;

public enum Planet {
    MERCURY (3.303e+23, 2.4397e6),
    VENUS   (4.869e+24, 6.0518e6),
    EARTH   (5.976e+24, 6.37814e6),
    MARS    (6.421e+23, 3.3972e6),
    JUPITER (1.9e+27,   7.1492e7),
    SATURN  (5.688e+26, 6.0268e7),
    URANUS  (8.686e+25, 2.5559e7),
    NEPTUNE (1.024e+26, 2.4746e7);

    // in kilograms
    private final double mass;
    // in meters
    private final double radius;
    
    private int chnlId;
    
    public int getChnlId() {
		return chnlId;
	}
	public void setChnlId(int chnlId) {
		this.chnlId = chnlId;
	}
	Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }
    private double mass() { return mass; }
    private double radius() { return radius; }

    // universal gravitational 
    // constant  (m3 kg-1 s-2)
    public static final double G = 6.67300E-11;

    double surfaceGravity() {
        return G * mass / (radius * radius);
    }
    double surfaceWeight(double otherMass) {
        return otherMass * surfaceGravity();
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Planet <earth_weight>");
            System.exit(-1);
        }
        double earthWeight = Double.parseDouble(args[0]);
        double mass = earthWeight/EARTH.surfaceGravity();
        System.out.println("Mercury: "+MERCURY.ordinal()+" venus: "+VENUS.ordinal());
        for (Planet p : Planet.values())
           System.out.printf("Your weight on %s is %f%n",
                             p, p.surfaceWeight(mass));
        
        Planet.MERCURY.setChnlId(56);
        Planet.VENUS.setChnlId(34);
        System.out.println("Mercury chan id "+Planet.MERCURY.getChnlId());
        System.out.println("Venus chan id "+Planet.VENUS.getChnlId());
        Map<Planet, Integer> mapTest = new HashMap<Planet, Integer>();
        mapTest.put(Planet.MERCURY, 111);
        mapTest.put(Planet.VENUS, 222);
        mapTest.put(Planet.EARTH, 333);
        System.out.println(mapTest.entrySet());
        char onPhs = 'A';
        onPhs++;
        onPhs++;
        System.out.println("Next val? "+ onPhs);
    }
}