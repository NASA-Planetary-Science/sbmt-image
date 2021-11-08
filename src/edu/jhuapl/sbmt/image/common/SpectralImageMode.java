package edu.jhuapl.sbmt.image.common;

public enum SpectralImageMode
{
    MONO {
        public String toString()
        {
            return "Monospectral";
        }
    },
    MULTI {
        public String toString()
        {
            return "Multispectral";
        }
    },
    HYPER {
        public String toString()
        {
            return "Hyperspectral";
        }
    },
}