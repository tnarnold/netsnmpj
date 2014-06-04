
/**
 * utility script for getting local properties for build variables
 */
class propertyquery {
    public static void main(String args[]) {
	System.out.println(System.getProperty(args[0]).toLowerCase()) ;
    }
}

/*
 * $Log: propertyquery.java,v $
 * Revision 1.1.1.1  2003/02/07 23:56:54  aepage
 * Migration Import
 *
 * Revision 1.2  2003/02/07 22:23:03  aepage
 * pre sourceforge.net migration checkins
 *
 */
