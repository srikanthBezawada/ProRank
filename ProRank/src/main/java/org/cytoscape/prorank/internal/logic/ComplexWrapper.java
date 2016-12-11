package org.cytoscape.prorank.internal.logic;

public class ComplexWrapper {
    private Complex complex;
    public int count;
    
    public ComplexWrapper(Complex complex, int count) {
        this.complex = complex;
        this.count = count;
    }
    
    public Complex getComplex() {
        return complex;
    }
    
    @Override
    public boolean equals(Object otherComplexWrapper) {
        if (!(otherComplexWrapper instanceof ComplexWrapper)) {
            return false;
        } 
        
        ComplexWrapper otherComplexWrapperRef = (ComplexWrapper)otherComplexWrapper;
        return otherComplexWrapperRef.count == this.count;
    }
    
    @Override
    public int hashCode() {
        return count;
    }
}
