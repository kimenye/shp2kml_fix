import groovy.xml.*

process("All_Roads_Msa.kml","All_Roads_Msa.extended.kml")


def process(input,output) {
	def file = new File(input)
	def placemarks = new XmlSlurper().parse(file)
	
	def mb = new groovy.xml.StreamingMarkupBuilder()
	new OutputStreamWriter(new FileOutputStream(output),'utf-16') << mb.bind {
	    mkp.xmlDeclaration()
		mkp.declareNamespace( kml: "http://earth.google.com/kml/2.0" )
	  
		kml {
			Document {
				Folder {
					name(placemarks.Document.name)
					Schema {
						SimpleField(name:"OBJECTID", type:"int")
						SimpleField(name:"NAME", type:"String")
						SimpleField(name:"RD_SUFFIX", type:"String")
						SimpleField(name:"CITY_NAME", type:"CITY_NAME")
					}
					placemarks.Document.Folder.Placemark.eachWithIndex { mark, idx ->
						Placemark {
							ExtendedData {
								SchemaData {
									SimpleData(getValue(mark.description.toString(), "OBJECTID"), name:"OBJECTID")
									SimpleData(mark.name, name:"NAME")
									SimpleData(getValue(mark.description.toString(), "RD_SUFFIX"), name:"RD_SUFFIX")
									SimpleData(getValue(mark.description.toString(), "CITY_NAME"), name:"CITY_NAME")
								}
							}
							LineString{
								coordinates(mark.LineString.coordinates.text())
							}
						}
					}
				}
			}
		}
	}
}

def clean(str) {
	str = str.replaceAll("border=0", "border=\"0\"")
	str = str.replaceAll("cellpadding=0", "cellpadding=\"0\"")
	str = str.replaceAll("cellspacing=0", "cellspacing=\"0\"")
	str = str.replaceAll("width=250", "width=\"250\"")
	str
}

def getValue(str, name) {
	str = clean(str)
	def slurper = new XmlSlurper()
	def rows = slurper.parseText(str).tr
	def res
	def items
	rows.eachWithIndex { it, i ->
		if (it.td[0].font.b.text() == name )
		{
/*			println "Found ${name} = ${it.td[1].font}"*/
			res = it.td[1].font.text()
		}
	}
	res
}