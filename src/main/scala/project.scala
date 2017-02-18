package appbuilder

////////////////////////////////////////////////////////////////////

import scala.xml._

import java.io._
import java.nio.file._

import scala.io.Source

import org.apache.commons.io.FileUtils._

////////////////////////////////////////////////////////////////////

object Project
{
	var name:String = null
	var path:String = null
	var scalaversion:String = ""
	var sbtversion:String = ""
	var rootdir:String = ""
	var githubuser:String = ""
	var githubdeltoken:String = ""
	var githubmail:String = ""
	var githubconfigdir:String = ""
	var githubdescription:String = ""
	var writeprotected:Boolean = false

	def set_path(setpath:String)
	{
		path=setpath
	}

	def clear
	{
		name = null
		path = null
		scalaversion = ""
		sbtversion = ""
		rootdir = ""
		githubuser = ""
		githubdeltoken = ""
		githubmail = ""
		githubconfigdir = ""
		githubdescription = ""
		writeprotected = false

		update
	}

	def createnew
	{
		clear

		name = "test"
		scalaversion = "2.11.8"
		sbtversion = "0.13.11"

		update
	}

	def open
	{
		load

		update
	}

	def isvalid = ( name != null )

	def url = s"""http://github.com/$githubuser/$name"""

	def update
	{
		var content="No project."

		val pathc=if(path!=null) path else "-"

		if(isvalid)
		{
			content=s"""
				|Project: $name ( scalaversion : $scalaversion , sbtversion : $sbtversion )<br>
				|File: $pathc
			""".stripMargin
		}

		Builder.LoadWebContent("{projectwebview}",content)

		Builder.GetMyEditableText("{name}").SetText(name)
		Builder.GetMyEditableText("{scalaversion}").SetText(scalaversion)
		Builder.GetMyEditableText("{sbtversion}").SetText(sbtversion)

		Builder.GetMyDirectoryChooser("{rootdir}").SetDir(rootdir)

		Builder.GetMyEditableText("{githubuser}").SetText(githubuser)
		Builder.GetMyEditableText("{githubdeltoken}").SetText(githubdeltoken)
		Builder.GetMyEditableText("{githubmail}").SetText(githubmail)
		Builder.GetMyEditableText("{githubdescription}").SetText(githubdescription)
		Builder.GetMyDirectoryChooser("{githubconfigdir}").SetDir(githubconfigdir)

		Builder.GetMyCheckBox("{writeprotected}").SetChecked(writeprotected)

		Builder.SetBoxDisable("{projectfields}",!isvalid)
		Builder.SetMenuDisable("{createmenu}",(!isvalid)||writeprotected)
		Builder.SetMenuDisable("{erasemenu}",(!isvalid)||writeprotected)
		Builder.SetMenuDisable("{gitmenu}",!isvalid)
		Builder.SetTabDisable("{srctab}",!isvalid)

		refresh_src
	}

	def exists = ( name != null )

	def toXml =
	{
		<project>
			<name>{name}</name>
			<scalaversion>{scalaversion}</scalaversion>
			<sbtversion>{sbtversion}</sbtversion>
			<rootdir>{rootdir}</rootdir>
			<githubuser>{githubuser}</githubuser>
			<githubdeltoken>{githubdeltoken}</githubdeltoken>
			<githubmail>{githubmail}</githubmail>
			<githubconfigdir>{githubconfigdir}</githubconfigdir>
			<githubdescription>{githubdescription}</githubdescription>
			<writeprotected>{writeprotected}</writeprotected>
		</project>
	}

	def toXmlString:String =
	{
		toXml.toString
	}	

	def fromXml(project:NodeSeq)
	{
		name = (project \ "name").text
		scalaversion = (project \ "scalaversion").text
		sbtversion = (project \ "sbtversion").text
		rootdir = (project \ "rootdir").text
		githubuser = (project \ "githubuser").text
		githubdeltoken = (project \ "githubdeltoken").text
		githubmail = (project \ "githubmail").text
		githubconfigdir = (project \ "githubconfigdir").text
		githubdescription = (project \ "githubdescription").text
		writeprotected = DataUtils.ParseBoolean((project \ "writeprotected").text,false)
	}

	def load
	{
		if(!((new File(path)).exists)) return

		val xml=scala.xml.XML.loadFile(path)

		fromXml(xml)
	}

	def save
	{
		if(path==null) return
		scala.xml.XML.save(path,toXml)
	}

	def check
	{

	}

	def sync
	{
		check

		update

		save
	}

	def refresh_src
	{
		if(!isvalid)
		{
			Builder.LoadWebContent("{srcwebview}","No project.")

			return
		}

		val files=DataUtils.CollectFiles(Project.mainscalasrcpath,recursive=true)

		val l=Project.mainscalasrcpath.length
		var i=0
		val filescontent=(for(file <- files) yield
		{
			i+=1
			val fc=file.substring(l+1)			
			var p=""
			var n=fc
			val ls=fc.lastIndexOf(sep)
			if(ls>=0)
			{
				p=fc.substring(0,ls)
				n=fc.substring(ls+1)
			}
			s"""
				|<tr>
				|<td>$i.</td>
				|<td onmousedown="setclick('${DataUtils.EncodeHex(file)}');">
				|<span style="cursor: pointer;">				
				|<font color="red">$p</font> 
				|<font color="blue"><b>$n</b></font>
				|</span>
				|</td>
				|</tr>
			""".stripMargin
		}).mkString("\n")

		val content=s"""
			|<script>
			|var file="";
			|function setclick(setfile)
			|{
			|	file=setfile;
			|}
			|</script>
			|<table>
			|$filescontent
			|</table>
		""".stripMargin

		Builder.LoadWebContent("{srcwebview}",content)
	}

	def path_from_list(dirs:List[String]):String = DataUtils.PathFromList(dirs)
	def file_from_list(dirs:List[String]):java.io.File = DataUtils.FileFromList(dirs)

	def sep = DataUtils.Sep

	def create_dirs
	{
		file_from_list(List(rootdir,name)).mkdir()
		file_from_list(List(rootdir,name,"src")).mkdir()
		file_from_list(List(rootdir,name,"stuff")).mkdir()
		for(mt <- List("main","test"))
		{
			file_from_list(List(rootdir,name,"src",mt)).mkdir()
			for(jsr <- List("java","scala","resources"))
				file_from_list(List(rootdir,name,"src",mt,jsr)).mkdir()
		}
		file_from_list(List(rootdir,name,"project")).mkdir()
	}

	def githubupdate(push:Boolean=true,pause:Boolean=true) = s"""
		|cd "${folderpath}"
		|copy gitconfig.txt "$githubconfigdir$sep.gitconfig"		
		|git init
		|git add -A .
		|git commit -m "Update"
		|git remote set-url origin https://github.com/$githubuser/$name
		|${if(push) "git push origin master" else ""}
		|${if(pause) "pause" else ""}
	""".stripMargin

	def githubfirst(push:Boolean=true,pause:Boolean=true) = s"""
		|cd "${folderpath}"
		|copy gitconfig.txt "$githubconfigdir$sep.gitconfig"
		|git init
		|git add -A .
		|git commit -m "First commit"
		|git remote add origin https://github.com/$githubuser/$name
		|${if(push) "git push origin master" else ""}
		|${if(pause) "pause" else ""}
	""".stripMargin

	def eghdescjson = s"""
		|{
		|"name":"$name",
		|"description":"${Project.githubdescription}"
		|}
	""".stripMargin

	def create_eghdescjson
	{
		DataUtils.WriteStringToFile(path_from_list(List(stuffpath,"eghdescjson.txt")),eghdescjson)
	}

	def eghdescsyncbat = s"""
		|cd "${Project.stuffpath}"
		|curl -X PATCH --user "${Project.githubuser}" --data-binary "@eghdescjson.txt" https://api.github.com/repos/${Project.githubuser}/${Project.name}
		|pause
	""".stripMargin	

	def buildsbt = s"""
		|
		|import com.github.retronym.SbtOneJar._
		|
		|oneJarSettings
		|
		|resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
		|
		|libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.4"
		|
		|libraryDependencies += "commons-io" % "commons-io" % "2.5"
		|
		|libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
		|
		|libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
		|
		|libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
		|
		|libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.4"
		|
		|scalacOptions ++= Seq("-feature")
		|
		|val ps = new sys.SystemProperties
		|val jh = ps("java.home")
		|val jdkh = jh.replaceAll("jre","jdk")
		|val f_jdkh = file(jdkh)
		|val f_libextjavafx = file(jh) / "lib/ext/jfxrt.jar"
		|
		|javaHome := Some(f_jdkh)
		|
		|unmanagedJars in Compile +=
		|{
		|	println("javaHome: "+f_jdkh+"\\nunmanagedJars+: "+f_libextjavafx)
		|	Attributed.blank(f_libextjavafx)
		|}
		|
		|addCommandAlias("c","~compile")
		|
		|name := "$name"
		|
		|version := "1.0"
		|
		|scalaVersion := "$scalaversion"
		|
	""".stripMargin

	def gitignore = s"""
		|
		|target/
		|
		|*.xml
		|*.txt
		|*.bat
		|
		|!ReadMe.txt
		|
	""".stripMargin

	def javajarrun = s"""
		|java -Xms4g -Xmx4g -jar $name.jar
		|pause
	""".stripMargin

	def onejarrun = s"""
		|
		|call sbt one-jar
		|
		|rem pause
		|
		|move target${sep}scala-2.11${sep}${name}_2.11-1.0-one-jar.jar $name.jar
		|
		|pause
		|
	""".stripMargin

	def readme = s"""
		|Project : $name.
	""".stripMargin

	def buildproperties = s"""
		|
		|sbt.version=$sbtversion
		|
	""".stripMargin

	def plugins = s"""
		|
		|addSbtPlugin("org.scala-sbt.plugins" % "sbt-onejar" % "0.8")
		|
	""".stripMargin

	def sbtrun = s"""
		|
		|call sbt run
		|
		|pause
		|
	""".stripMargin

	def sbtrunuse = s"""
		|
		|call sbt run
		|
	""".stripMargin

	def createrepo = s"""
		|curl --user "$githubuser" --data "{\\"name\\":\\"$name\\"}" https://api.github.com/user/repos
		|pause
	""".stripMargin

	def deleterepoauth = s"""
		|curl --user "$githubuser" --data "{\\"scopes\\":[\\"delete_repo\\"], \\"note\\":\\"token with delete repo scope\\"}" https://api.github.com/authorizations
		|pause
	""".stripMargin

	def deleterepo = s"""
		|curl -X DELETE -H "Authorization: token $githubdeltoken" https://api.github.com/repos/$githubuser/$name
		|pause
	""".stripMargin

	def gitconfig = s"""
		|[user]
		|	name = $githubuser
		|	email = $githubmail
		|	[credential "https://github.com/$githubuser/$name"]
		|		username = $githubuser
		|[filter "hawser"]
		|	clean = git hawser clean %f
		|	smudge = git hawser smudge %f
		|	required = true
	""".stripMargin

	def populate_build
	{
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"build.sbt")),buildsbt)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,".gitignore")),gitignore)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"ReadMe.txt")),readme)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"project","build.properties")),buildproperties)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"project","plugins.sbt")),plugins)
	}

	def populate_bats
	{
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"sbtrun.bat")),sbtrun)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"sbtrun.use.bat")),sbtrunuse)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"onejarrun.bat")),onejarrun)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"javajarrun.bat")),javajarrun)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"createrepo.bat")),createrepo)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"deleterepoauth.bat")),deleterepoauth)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"deleterepo.bat")),deleterepo)		
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"githubfirst.bat")),githubfirst())
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"githubupdate.bat")),githubupdate())
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"gitconfig.txt")),gitconfig)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"stuff","eghdescsync.bat")),eghdescsyncbat)
	}

	def populate
	{
		populate_build
		
		populate_bats

		create_source
	}

	def create_source
	{
		file_from_list(List(rootdir,name,"src","main","scala","guisystem")).mkdir()
		for(sf <- List("builder.scala","data.scala","mycomponent.scala","actor.scala","processmanager.scala"))
		{
			val lines=DataUtils.ReadFileToString(path_from_list(List("src","main","scala","guisystem",sf))).split("\\r?\\n").toList
			val content=s"package $name\r\n"+lines.tail.mkString("\r\n")
			DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"src","main","scala","guisystem",sf)),content)
		}

		val main=s"""
			|package $name
			|
			|import javafx.application._
			|
			|object Start
			|{
			|
			|	def main(args: Array[String])
			|	{
			|
			|		Application.launch(classOf[GuiClass], args: _*)
			|
			|	}
			|
			|}
			|
		""".stripMargin

		val myapp=s"""
			|package $name
			|
			|import javafx.stage._
			|
			|import Builder._
			|
			|object MyApp
			|{
			|
			|	def Init
			|	{
			|		ModuleManager.Add(MyActor)
			|		ModuleManager.Add(ProcessManager)
			|	}
			|
			|	def handler(ev:MyEvent)
			|	{
			|
			|	}
			|
			|	def Start(primaryStage:Stage)
			|	{
			|		val blob=\"\"\"
			|			|<vbox>
			|			|<tabpane id="{maintabpane}">
			|			|<tab caption="Main">
			|			|</tab>
			|			|<tab caption="Systemlog">
			|			|<webview id="{systemlog}"/>
			|			|</tab>
			|			|<tab caption="Execqueue">
			|			|<webview id="{execqueue}"/>
			|			|</tab>
			|			|<tab caption="Queuelog">
			|			|<webview id="{execqueuelog}"/>
			|			|</tab>
			|			|</tabpane>
			|			|</vbox>
			|		\"\"\".stripMargin
			|
			|		MyStage(id="{mainstage}",s=primaryStage,title="$name",blob=blob,handler=handler)
			|	}
			|	
			|	def Stop
			|	{
			|
			|	}	
			|
			|}
			|
		""".stripMargin

		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"src","main","scala",s"$name.scala")),main)
		DataUtils.WriteStringToFile(path_from_list(List(rootdir,name,"src","main","scala","myapp.scala")),myapp)

	}

	def create_all
	{
		create_dirs

		populate
	}

	def folderpath = path_from_list(List(rootdir,name))
	def stuffpath = path_from_list(List(rootdir,name,"stuff"))
	def gitpath = path_from_list(List(rootdir,name,".git"))
	def mainscalasrcpath = path_from_list(List(rootdir,name,"src","main","scala"))

	def erase_git
	{
		var efr=DataUtils.EraseFiles(gitpath,dofiles=true,dodirs=true,recursive=true)

		DataUtils.DeleteDir(gitpath)
		efr.nodirs+=1

		MyApp.ReportEraseFilesResult(efr)
	}

	def erase_files
	{
		val efr=DataUtils.EraseFiles(folderpath,dofiles=true,dodirs=false,recursive=true)

		MyApp.ReportEraseFilesResult(efr)
	}

	def erase_dirs =
	{
		var efr=DataUtils.EraseFiles(folderpath,dofiles=false,dodirs=true,recursive=true)

		DataUtils.DeleteDir(folderpath)

		efr.nodirs+=1

		MyApp.ReportEraseFilesResult(efr)
	}

	def erase_all
	{
		var efr=DataUtils.EraseFiles(folderpath,dofiles=true,dodirs=true,recursive=true)

		DataUtils.DeleteDir(folderpath)

		efr.nodirs+=1

		MyApp.ReportEraseFilesResult(efr)
	}

}