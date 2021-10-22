package edu.jhuapl.sbmt.image.impl;

public class ValidityCheckerDoubleFactory
{

    /**
     * Checker for validity of scalar double values, based on the location
     * and/or the value at that location.
     */
    @FunctionalInterface
    public interface ScalarValidityChecker extends ValidityChecker
    {
        boolean test(int i, int j, double value);
    }

    /**
     * Checker for validity of vector double values, based on the location
     * and/or the value at that location.
     */
    @FunctionalInterface
    public interface VectorValidityChecker extends ValidityChecker
    {
        boolean test(int i, int j, int k, double value);
    }

    public ValidityCheckerDoubleFactory()
    {
        super();
    }

    public ScalarValidityChecker of(double... invalidValues)
    {

        return (i, j, value) -> {
            for (double invalidValue : invalidValues)
            {
                if (Double.compare(value, invalidValue) == 0)
                {
                    return false;
                }
            }

            return true;
        };

    }

}
