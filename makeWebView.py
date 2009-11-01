# Copyright (c) 2002-2004 Ivan Moore 
# This code is under the same license as Jester
# see http://jester.sourceforge.net

from xml.parsers import expat
import sys, cgi
from os.path import basename, splitext
import getopt

class JesterIndexFileWriter:
	def __init__(self, indexFileName, sort, ignoreZeroChangeFiles, ignorePerfectScoreFiles): 
		self.indexfile = open(indexFileName,"w")
		self.indexfile.write("<html><head><title>Jester Reports</title>\n")
		self.indexfile.write("<style>\n")
		self.indexfile.write(".red {color: #FF0000}\n")
		self.indexfile.write(".green {color: #009900}\n")
		self.indexfile.write("</style></head>\n")
		self.indexfile.write("<body><h1>The files Jester made changes to:</h1>\n")
		self.indexfile.write('<table BORDER WIDTH="100%">\n')
		self.indexfile.write("<tr><td>file name</td><td>score</td><td>number of changes where tests still passed</td><td>total number of changes</td></tr>\n")
		
		self.sort = sort
		self.ignoreZeroChangeFiles = ignoreZeroChangeFiles
		self.ignorePerfectScoreFiles = ignorePerfectScoreFiles
		self.mutatedFileList = []
	def close(self):
		self._writeMutatedFileIndexes()
		self.indexfile.write("\n</table>\n")
		self.indexfile.write("</body></html>")
		self.indexfile.close()
	def addMutatedFileIndex(self, pagename, isAbsolute, score, numberOfChangesThatDidNotCauseTestsToFail, numberOfChanges):
		self.mutatedFileList.append((pagename, isAbsolute, score, numberOfChangesThatDidNotCauseTestsToFail, numberOfChanges))
	def _writeMutatedFileIndexes(self):
		if self.sort:
			self.mutatedFileList.sort()
			
		for pagename, isAbsolute, score, numberOfChangesThatDidNotCauseTestsToFail, numberOfChanges in self.mutatedFileList:
			if self.ignoreZeroChangeFiles and numberOfChanges=='0':
				continue
			if self.ignorePerfectScoreFiles and numberOfChangesThatDidNotCauseTestsToFail=='0' and numberOfChanges!='0':
				continue
			fileProtocolIfAbsolute = ""
			if isAbsolute:
				fileProtocolIfAbsolute = "file://"
			self.indexfile.write('<tr><td><a href="'+fileProtocolIfAbsolute+pagename+'">'+pagename+'</a>')
			self.indexfile.write('</td><td><span class="green">'+str(score)+'</span>')
			self.indexfile.write('</td><td><span class="red">'+str(numberOfChangesThatDidNotCauseTestsToFail)+'</span>')
			self.indexfile.write('</td><td><span class="green">'+str(numberOfChanges)+'</span></td></tr>')
	
class ChangesParser:
	def __init__(self, indexFileName, jesterIndexFileWriter):
		self.parser = expat.ParserCreate()
		self.parser.StartElementHandler = self.startElement
		self.parser.EndElementHandler = self.endElement
		self.indexWriter = jesterIndexFileWriter
	def feed(self, data):
		self.parser.Parse(data,0)
	def close(self):
		self.parser.Parse("",1)
		del self.parser
		self.indexWriter.close()
	def endElement(self, tag):
		if tag=="JestedFile":
			self.write(cgi.escape(self.originalContents[self.indexInOriginalOfLastChange:]))
			self.writeHTMLend()
			self.allChangesFile.close()
	def write(self, string):
		self.allChangesFile.write(string.replace('\r\n','\n'))
	def writeHTMLstart(self):
		self.write("<html><head><title>Changes Jester made to ")
		self.write(basename(self.filename))
		self.write(" that did not cause the tests to fail.</title>\n")
		self.write("<style>\n")
		self.write(".added {color: #FF0000}\n")
		self.write(".removed {text-decoration: line-through; color: #009900}\n")
		self.write("</style></head>\n")
		self.write('<body><h1>Changes to '+self.filename+'</h1>\n')
		self.write('<pre>\n')
	def writeHTMLend(self):
		self.write('\n</pre></body></html>')
	def startElement(self, tag, attrs):
		if tag=="JestedFile":
			filename = str(attrs["fileName"])
			self.filename = filename
			self.originalContents = open(filename,"rb").read()
			pagename = (splitext(filename)[0]+".html").replace('\\','/')
			isAbsolute = attrs["fileName"] == attrs["absolutePathFileName"]
			self.indexWriter.addMutatedFileIndex(pagename, isAbsolute, attrs["score"], attrs["numberOfChangesThatDidNotCauseTestsToFail"], attrs["numberOfChanges"])
			self.allChangesFile = open(pagename,"w")
			self.writeHTMLstart()
			self.indexInOriginalOfLastChange = 0
		elif tag=="ChangeThatDidNotCauseTestsToFail":
			indexOfChange = int(attrs["index"])
			if indexOfChange>=self.indexInOriginalOfLastChange:
				originalUptoChange = self.originalContents[self.indexInOriginalOfLastChange:indexOfChange]
				self.write(cgi.escape(originalUptoChange))
				self.write('<span class="removed">'+attrs["from"]+'</span>')
			self.write('<span class="added">'+attrs["to"]+'</span>')
			self.indexInOriginalOfLastChange = indexOfChange + len(attrs["from"])
	
def makeWebView(changesFileName, indexFileName, sort, ignoreZeroChangeFiles, ignorePerfectScoreFiles):
	file = open(changesFileName)
	contents = file.read()
	file.close()

	p = ChangesParser(indexFileName, JesterIndexFileWriter(indexFileName, sort, ignoreZeroChangeFiles, ignorePerfectScoreFiles))
	p.feed(contents)
	p.close()
	
def usage():
	print """python makeWebView.py [options]
Options:
  -h              print this message
  -s              sort the index file by file name
  -z              do not include files which Jester made no changes to in index
  -p              do not include files which had a perfect score in index
  -i <filename>   use this for the "index" file name; default="jester.html"
  -x <filename>   get change list from this file; default="jesterReport.xml"

Note; "index file" is the html summary file, usually called "jester.html", that links to files showing actual changes"""

if __name__ == '__main__':
    try:
        opts, args = getopt.getopt(sys.argv[1:], "x:i:hszp")
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    changesFileName = "jesterReport.xml"
    indexFileName = "jester.html"
    sort = False
    ignoreZeroChangeFiles = False
    ignorePerfectScoreFiles = False
    for o, a in opts:
        if o=="-h":
            usage()
            sys.exit()
        if o=="-x":
            changesFileName = a
        if o=="-i":
            indexFileName = a
        if o=="-s":
        	sort = True
        if o=="-z":
        	ignoreZeroChangeFiles = True
        if o=="-p":
        	ignorePerfectScoreFiles = True
            
    makeWebView(changesFileName, indexFileName, sort, ignoreZeroChangeFiles, ignorePerfectScoreFiles)