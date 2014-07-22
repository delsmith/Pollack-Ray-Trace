package jray.evol;

/**An Evaluator determines the fitness of an individual.*/
public interface Evaluator
{
    public double evaluate(Object individual);
}
