---

## EA-OCR Application Guide:
**1. Installations:** 

These installations are mandatory:  **Windows** 
1. Install [VC++ 2015](https://www.microsoft.com/en-au/download/details.aspx?id=53840) - It is require to run Tesseract

2. Install [JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

3. Install [Maven](http://maven.apache.org/) more help [here..](https://www.mkyong.com/maven/how-to-install-maven-in-windows/)

4. Install [GhostScript](https://ghostscript.com/download/gsdnld.html) - PDF to images converter - I used gs923w64.exe

5. Install [ImageMagick](https://imagemagick.org/script/download.php#windows) - Image processing - I used ImageMagick-7.0.7-28-Q16-x64-dll.exe **Choose legacy utilities for ImageMagic** (Check box on install screen)
	
6. Install [Tesseract](https://digi.bib.uni-mannheim.de/tesseract/) - Read text from Images - I used [tesseract-ocr-setup-4.0.0-alpha.20180109.exe](https://github.com/tesseract-ocr/tesseract/wiki/4.0-with-LSTM) **Choose Additional language data; select all Indian languages** (Expand the option, deselect all and select the languages you want)

**2. Pull code from repository**

`$ git clone https://bitbucket.org/aap-elect-analysis/ea-ocr.git`

**3. Update configurations**  
Update the installation paths to `src/main/resources/ea.ocr.properties`

Update Imagemagick, Tesseract and other file processing proprties to `src/main/resources/{state}.json`
Ex. `src/main/resources/haryana.json` 

**4. Build the jar and Start application** 
`{root-dir}\ea-ocr>mvn clean install -Dfile.encoding=UTF-8`

**5. Start files processing**

1. Give read/write permissions to `ea-ocr` directory

2. Check the jar version in `target` directory, use it in below below command and execute it-

`{root-dir}\ea-ocr>java -Dfile.encoding=UTF-8 -jar -Xms2048m -Xmx4096m target/ea-ocr-{jar-version}.jar ea-ocr`
 
3. [click here to start..](http://localhost:8090/swagger-ui.html) the ea-ocr-controller  --> POST 

4. Enter values - pdfFilePath and state(This value should match to filename in src/main/resources/haryana.json)

Know [swagger](http://localhost:8090/v2/api-docs)   

**6. Start multiple instances of application (Scale the application Horizontally)**
1.  Copy the entire workspace `ea-ocr` in multiple directories `{root-dir}\EA\1\ea-ocr`,  `{root-dir}\EA\2\ea-ocr`, `{root-dir}\EA\3\ea-ocr` etc.

2.  Change the port number in each workspace on path `src/main/resources/application.properties` to `server.port=8090`, `server.port=8091`, `server.port=8092` etc

3. Follow the **step:4** and **step:5** to build and deploy the applications.

4. Use application using Urls -

 i.   http://localhost:8090/swagger-ui.html 
 
 ii.  http://localhost:8091/swagger-ui.html
 
 iii. http://localhost:8092/swagger-ui.html

Note: Multiple applications could be start simultaneously, just make sure the RAM size is enough to run multiple applications.
---

## Help:

**Edit a file, create a new file, and clone from Bitbucket in under 2 minutes**

When you're done, you can delete the content in this README and update the file with details for others getting started with your repository.

*We recommend that you open this README in another tab as you perform the tasks below. You can [watch our video](https://youtu.be/0ocf7u76WSo) for a full demo of all the steps in this tutorial. Open the video in a new tab to avoid leaving Bitbucket.*

---

## Edit a file

You’ll start by editing this README file to learn how to edit a file in Bitbucket.

1. Click **Source** on the left side.
2. Click the README.md link from the list of files.
3. Click the **Edit** button.
4. Delete the following text: *Delete this line to make a change to the README from Bitbucket.*
5. After making your change, click **Commit** and then **Commit** again in the dialog. The commit page will open and you’ll see the change you just made.
6. Go back to the **Source** page.

---

## Create a file

Next, you’ll add a new file to this repository.

1. Click the **New file** button at the top of the **Source** page.
2. Give the file a filename of **contributors.txt**.
3. Enter your name in the empty file space.
4. Click **Commit** and then **Commit** again in the dialog.
5. Go back to the **Source** page.

Before you move on, go ahead and explore the repository. You've already seen the **Source** page, but check out the **Commits**, **Branches**, and **Settings** pages.

---

## Clone a repository

Use these steps to clone from SourceTree, our client for using the repository command-line free. Cloning allows you to work on your files locally. If you don't yet have SourceTree, [download and install first](https://www.sourcetreeapp.com/). If you prefer to clone from the command line, see [Clone a repository](https://confluence.atlassian.com/x/4whODQ).

1. You’ll see the clone button under the **Source** heading. Click that button.
2. Now click **Check out in SourceTree**. You may need to create a SourceTree account or log in.
3. When you see the **Clone New** dialog in SourceTree, update the destination path and name if you’d like to and then click **Clone**.
4. Open the directory you just created to see your repository’s files.

Now that you're more familiar with your Bitbucket repository, go ahead and add a new file locally. You can [push your change back to Bitbucket with SourceTree](https://confluence.atlassian.com/x/iqyBMg), or you can [add, commit,](https://confluence.atlassian.com/x/8QhODQ) and [push from the command line](https://confluence.atlassian.com/x/NQ0zDQ).