package com.example.headers;

import prot.soap.SoapHeaderContributor;

/**
 * Example dynamic header provider for calculator service
 */
public class CalculatorDynamicHeader implements SoapHeaderContributor {
    @Override
    public void injectIntoSoapHeader(Object soapHeader) {}
}

