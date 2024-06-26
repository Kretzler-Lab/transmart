/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/*
Copyright (C) 2001-2004  Kyle Siegrist, Dawn Duehring
Department of Mathematical Sciences
University of Alabama in Huntsville

This program is part of Virtual Laboratories in Probability and Statistics,
http://www.math.uah.edu/stat/.

This program is licensed under a Creative Commons License. Basically, you are free to copy,
distribute, and modify this program, and to make commercial use of the program.
However you must give proper attribution.
See http://creativecommons.org/licenses/by/2.0/ for more information.
 */

package com.recomdata.util;

/**
 * Models an abstract implementation of a real probability distribution.
 *
 * @author Kyle Siegrist
 * @author Dawn Duehring
 */
public abstract class Distribution{
    public static final int DISCRETE = 0;
    public static final int CONTINUOUS = 1;
    public static final int MIXED = 2;

    private int type;
    private Domain domain;

    /**
     * Defines the probability density function of the distribution.
     *
     * @param x a number in the domain of the distribution
     */
    public abstract double getDensity(double x);

    /**
     * Sets the domain of the distribution for purposes of data collection
     * and for default computations.  For a discrete distribution, the domain specifies
     * the values on which the distribution is defined (although truncated if the true set
     * of values is infinite). For a continuous distribution, domain defines the interval
     * on which the distribuiton is defined (truncated if the true interval is infinite).
     *
     * @param d the domain
     */
    protected void setDomain(Domain d){
        domain = d;
        type = domain.getType();
    }

    /**
     * Sets the domain of the distribution for purposes of data collection
     * and for default computations.
     *
     * @param a lower value or bound of the domain
     * @param b the upper value or bound of the domain
     * @param w the width (step size) of the domain
     * @param t the type of domain (DISCRETE or CONTINUOUS)
     */
    public void setDomain(double a, double b, double w, int t){
        setDomain(new Domain(a, b, w, t));
    }

    /**
     * The domain of the distribution.
     *
     * @return the domain
     */
    public Domain getDomain(){
        return domain;
    }

    /**
     * The type of the distribution (discrete or continuous).
     *
     * @return the type
     */
    public final int getType(){
        return type;
    }

    /**
     * The largest (finite) value of the probability density function on the
     * finite set of domain values. This method should be overridden if the
     * maximum value is known in closed form.
     *
     * @return the maximum value of the probability density function
     */
    public double getMaxDensity(){
        double max = 0;
        for (int i = 0; i < domain.getSize(); i++){
            double d = getDensity(domain.getValue(i));
            if (d > max & d < Double.POSITIVE_INFINITY) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Returns a default approximation of the moment of a specified order about
     * a specified point.  This method should be overriden if the moment is
     * known in closed form.
     *
     * @param a the center
     * @param n the order
     * @return the moment of order n about a
     */
    public double getMoment(double a, int n){
        double sum = 0;
        double w;
        if (type == DISCRETE) {
            w = 1;
        }
        else {
            w = domain.getWidth();
        }
        for (int i = 0; i < domain.getSize(); i++){
            double x = domain.getValue(i);
            sum = sum + Math.pow(x - a, n) * getDensity(x) * w;
        }
        return sum;
    }

    /**
     * A default approximation of the moment of a specified order about 0.
     * Should be overriden if the moment is known in closed form.
     *
     * @param n the order
     * @return the moment of order n
     */
    public double getMoment(int n){
        return getMoment(0, n);
    }

    /**
     * A default approximate mean. Should be overriden if the mean is known in
     * closed form.
     *
     * @return the mean
     */
    public double getMean(){
        return getMoment(1);
    }

    /**
     * A default approximate variance. This method should be overriden
     * if the variance is known in closed form.
     *
     * @return the variance
     */
    public double getVariance(){
        return getMoment(getMean(), 2);
    }

    /**
     * The standard deviation, as the square root of the variance.
     *
     * @return the standard deviation
     */
    public double getSD(){
        return Math.sqrt(getVariance());
    }

    /**
     * A default approximate cumulative distribution function.
     * This should be overriden if the CDF is known in closed form.
     *
     * @param x a number in the domain of the distribution
     * @return the cumulative probability at x
     */
    public double getCDF(double x){
        double w;
        if (type == DISCRETE) {
            w = 1;
        }
        else{
            w = domain.getWidth();
        }

        int j = domain.getIndex(x);
        if (j < 0) {
            return 0;
        }
        if (j >= domain.getSize()) {
            return 1;
        }

        double sum = 0;
        for (int i = 0; i <= j; i++) {
            sum = sum + getDensity(domain.getValue(i)) * w;
        }

        if (type == CONTINUOUS){
            double y = domain.getValue(j) - 0.5 * w;
            sum = sum + getDensity((x + y) / 2) * (x - y);
        }
        return sum;
    }

    /**
     * Computes an approximate quantile function.
     * This should be overriden if the quantile function is known in closed form.
     *
     * @param p a probability in (0, 1)
     * @return the quantile of order p
     */
    public double getQuantile(double p){
        if (type == DISCRETE){
            if (p <= 0) {
                return domain.getLowerValue();
            }

            if (p >= 1) {
                return domain.getUpperValue();
            }

            int n = domain.getSize();
            int i = 0;
            double x = domain.getValue(i);
            double q = getDensity(x);
            while ((q < p) & (i < n)){
                i++;
                x = domain.getValue(i);
                q = q + getDensity(x);
            }
            return x;
        }

        if (p <= 0) {
            return domain.getLowerBound();
        }

        if (p >= 1) {
            return domain.getUpperBound();
        }

        double x1 = domain.getLowerBound();
        double x2 = domain.getUpperBound();
        double x = (x1 + x2) / 2;
        double q = getCDF(x);
        double error = Math.abs(q - p);
        int n = 1;
        while (error > 0.0001 & n < 100){
            n++;
            if (q < p) {
                x1 = x;
            }
            else {
                x2 = x;
            }
            x = (x1 + x2) / 2;
            q = getCDF(x);
            error = Math.abs(q - p);
        }
        return x;
    }

    /**
     * Computes a default simulation of a value from the distribution,
     * as a random quantile. Should be overridden if a better method of
     * simulation is known.
     *
     * @return a simulated value from the distribution
     */
    public double simulate(){
        return getQuantile(Math.random());
    }

    /**
     * Computes a default approximate median. Should be overriden when there is
     * a closed form expression for the median.
     *
     * @return the median
     */
    public double getMedian(){
        return getQuantile(0.5);
    }

    /**
     * Computes the failure rate function.
     *
     * @param x a number in the domain of the distribution
     * @return the failure rate at x
     */
    public double getFailureRate(double x){
        return getDensity(x) / (1 - getCDF(x));
    }

    /**
     * Computes a default approximation to the moment generating function.
     * The moment generating function is the generating function often used for
     * continuous distributions. This method should be overriden if the MGF is known in
     * closed form.
     *
     * @param t a real number
     * @return the moment generating function at t
     */
    public double getMGF(double t){
        double w;
        if (type == DISCRETE) {
            w = 1;
        }
        else {
            w = domain.getWidth();
        }

        double sum = 0;
        for (int i = 0; i < domain.getSize(); i++){
            double x = domain.getValue(i);
            sum = sum + Math.exp(t * x) * getDensity(x) * w;
        }
        return sum;
    }

    /**
     * Computes a default approximation to the probability generating function.
     * The probability generating function is the generating function often used for
     * discrete, distributions on the non-negative integers. This method should be
     * overriden if the PGF is known in closed form.
     *
     * @param t a real number
     * @return the probability generating function at t
     */
    public double getPGF(double t){
        return getMGF(Math.log(t));
    }

    /**
     * @return the name of the distribution and the values of the parameters
     */
    public String toString(){
        return "Distribution distribution [type = " + type + ", domain = " + domain + "]";
    }
}
