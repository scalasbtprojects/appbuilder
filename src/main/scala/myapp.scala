package appbuilder

////////////////////////////////////////////////////////////////////

import javafx.application._
import javafx.stage._
import javafx.scene._
import javafx.scene.layout._
import javafx.scene.control._
import javafx.scene.canvas._
import javafx.scene.input._
import javafx.scene.paint._
import javafx.scene.text._
import javafx.scene.web._
import javafx.scene.image._
import javafx.event._
import javafx.geometry._
import javafx.beans.value._
import javafx.collections._

import collection.JavaConversions._

import java.io._
import scala.io._

import Builder._

import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.util._

import scala.concurrent.ExecutionContext.Implicits.global

import org.apache.commons.lang.time.DurationFormatUtils.formatDuration
import org.apache.commons.lang.time.DateFormatUtils._

import collection.JavaConverters._

import play.api.libs.json._

////////////////////////////////////////////////////////////////////

object MyApp
{
	def Init
	{
		ModuleManager.Add(MyActor)
		ModuleManager.Add(ProcessManager)
	}

	def handler(ev:MyEvent)
	{
		SetInfo("")

		if(ev.kind=="webview clicked")
		{
			if(ev.Id=="{srcwebview}")
			{
				val file=ExecuteWebScript("{srcwebview}","file")

				EditFile(file)
			}

			if(ev.Id=="{searchwebview}")
			{
				val file=ExecuteWebScript("{searchwebview}","file")

				EditFile(file)
			}
		}

		if(ev.kind=="checkbox changed")
		{
			if(ev.Id=="{writeprotected}")
			{
				Project.writeprotected=ev.value.toBoolean
				Project.sync
			}
		}

		if(ev.kind=="stage closed")
		{
			if(ev.Id=="{mainstage}")
			{
				CloseAllStages
			}
		}

		if(ev.kind=="textfield entered")
		{
			if(ev.Id=="{cmdtext}")
			{
				CmdExec
			}
		}

		if(ev.kind=="button pressed")
		{
			if(ev.Id=="{search}")
			{
				Search
			}

			if(ev.Id=="{startsbt}")
			{
				StartSbt
			}

			if(ev.Id=="{sbtrun}")
			{
				SbtRun
			}

			if(ev.Id=="{refreshsrc}")
			{
				RefreshSrc
			}			

			if(ev.Id=="{cmdexec}")
			{
				CmdExec
			}

			if(ev.Id=="{syncghdesc}")
			{
				SyncGhdesc
			}
		}

		if(ev.kind=="directory chosen")
		{
			if(ev.Id=="{rootdir}")
			{
				Project.rootdir=ev.value
				Project.sync
			}
			
			if(ev.Id=="{githubconfigdir}")
			{
				Project.githubconfigdir = ev.value
				Project.sync
			}
		}

		if(ev.kind=="menuitem clicked")
		{
			if(ev.Id=="{ziprepo}")
			{
				ZipRepo
			}			

			if(ev.Id=="{createbats}")
			{
				CreateBats
			}			

			if(ev.Id=="{createrepo}")
			{
				CreateRepo
			}			

			if(ev.Id=="{erasegit}")
			{
				Project.erase_git
			}			

			if(ev.Id=="{viewrepo}")
			{
				ViewRepo
			}			

			if(ev.Id=="{gitfirst}")
			{
				GitFirst
			}

			if(ev.Id=="{gitupdate}")
			{
				GitUpdate()
			}

			if(ev.Id=="{gitupdatecm}")
			{
				GitUpdate(cm=true)
			}

			if(ev.Id=="{gitpush}")
			{
				GitPush
			}

			if(ev.Id=="{erasefiles}")
			{
				Project.erase_files
			}

			if(ev.Id=="{erasedirs}")
			{
				Project.erase_dirs
			}

			if(ev.Id=="{eraseall}")
			{
				Project.erase_all
			}

			if(ev.Id=="{populate}")
			{
				Project.populate
			}

			if(ev.Id=="{createall}")
			{
				Project.create_all
			}

			if(ev.Id=="{createdirs}")
			{
				Project.create_dirs
			}

			if(ev.Id=="{new}")
			{
				Project.createnew
			}

			if(ev.Id=="{saveas}")
			{
				SaveProjectAs
			}

			if(ev.Id=="{open}")
			{
				OpenProject
			}

			if(ev.Id=="{close}")
			{
				Project.clear
			}
		}

		if(ev.kind=="text edited")
		{
			if(ev.Id=="{name}")
			{
				Project.name = ev.value
				Project.sync
			}

			if(ev.Id=="{scalaversion}")
			{
				Project.scalaversion = ev.value
				Project.sync
			}

			if(ev.Id=="{sbtversion}")
			{
				Project.sbtversion = ev.value
				Project.sync
			}

			if(ev.Id=="{githubuser}")
			{
				Project.githubuser = ev.value
				Project.sync
			}

			if(ev.Id=="{githubmail}")
			{
				Project.githubmail = ev.value
				Project.sync
			}

			if(ev.Id=="{githubdeltoken}")
			{
				Project.githubdeltoken = ev.value
				Project.sync
			}

			if(ev.Id=="{githubdescription}")
			{
				Project.githubdescription = ev.value
				Project.sync
			}
		}
	}

	var selectedmaintab=0

	var ps:Stage=null

	def Start(primaryStage:Stage)
	{
		ps=primaryStage

		val blob=s"""
			|<vbox gap="0">
			|
			|<menubar>
			|<menu text="File">
			|<menuitem id="{open}" text="Open Project"/>
			|<menuitem id="{new}" text="Create New Project"/>
			|<menuitem id="{saveas}" text="Save Project as"/>			
			|<menuitem id="{close}" text="Close Project"/>			
			|</menu>
			|<menu id="{createmenu}" text="Create">
			|<menuitem id="{createdirs}" text="Create directories"/>
			|<menuitem id="{populate}" text="Populate"/>
			|<menuitem id="{createall}" text="Create all"/>
			|</menu>
			|<menu id="{erasemenu}" text="Erase">			
			|<menuitem id="{erasefiles}" text="Erase files"/>
			|<menuitem id="{erasedirs}" text="Erase directories"/>
			|<menuitem id="{eraseall}" text="Erase all"/>
			|</menu>			
			|<menu id="{gitmenu}" text="Git">			
			|<menuitem id="{viewrepo}" text="View repo"/>
			|<menuitem id="{erasegit}" text="Erase .git"/>
			|<menuitem id="{createrepo}" text="Create repo"/>
			|<menuitem id="{gitfirst}" text="First commit"/>
			|<menuitem id="{gitupdate}" text="Update"/>
			|<menuitem id="{gitupdatecm}" text="Update with custom commit message"/>
			|<menuitem id="{gitpush}" text="Push"/>
			|<menuitem id="{ziprepo}" text="Zip repo"/>
			|<menuitem id="{createbats}" text="Create utility batch files"/>
			|</menu>
			|</menubar>
			|
			|<label id="{infolabel}"/>
			|
			|<webview id="{projectwebview}" height="50.0" width="800.0"/>
			|
			|<tabpane id="{maintabpane}">
			|<tab caption="Project">
			|<vbox>			
			|<vbox id="{projectfields}">
			|<hbox gap="30">
			|<editabletext id="{name}" lwidth="100.0" vwidth="100.0" name="Name"/>			
			|<editabletext id="{scalaversion}" lwidth="100.0" vwidth="60.0" name="Scala version"/>
			|<editabletext id="{sbtversion}" lwidth="100.0" vwidth="60.0" name="Sbt version"/>			
			|</hbox>
			|<hbox>
			|<label text="Root directory"/>
			|<directorychooser id="{rootdir}"/>			
			|</hbox>
			|<editabletext id="{githubuser}" lwidth="150.0" vwidth="150.0" name="Github username"/>
			|<editabletext id="{githubdeltoken}" lwidth="150.0" vwidth="300.0" name="Github deltoken"/>			
			|<editabletext id="{githubmail}" lwidth="150.0" vwidth="300.0" name="Github mail"/>
			|<hbox>
			|<editabletext id="{githubdescription}" lwidth="150.0" vwidth="300.0" name="Github description"/>
			|<button id="{syncghdesc}" text="Sync"/>
			|</hbox>
			|<hbox>
			|<label text="Github configdir"/>
			|<directorychooser id="{githubconfigdir}"/>			
			|</hbox>
			|<hbox>
			|<label text="Protected"/>
			|<checkbox id="{writeprotected}"/>			
			|</hbox>
			|</vbox>
			|</vbox>
			|</tab>
			|<tab caption="Web">
			|<webview id="{webwebview}" height="500.0" width="1100.0"/>
			|</tab>
			|<tab id="{srctab}" caption="Src">
			|<vbox>
			|<hbox>
			|<button id="{refreshsrc}" text="Refresh"/>
			|<button id="{startsbt}" text="Start sbt"/>
			|<button id="{sbtrun}" text="Sbt run"/>
			|</hbox>
			|<webview id="{srcwebview}" height="460.0" width="1100.0"/>
			|</vbox>
			|</tab>
			|<tab id="{searchtab}" caption="Search">
			|<vbox>
			|<directorychooser id="{searchdir}"/>
			|<editabletext id="{searchexts}" store="true" lwidth="100.0" vwidth="300.0" name="Extensions"/>			
			|<hbox>			
			|<editabletext id="{term}" store="true" lwidth="100.0" vwidth="300.0" name="Search for"/>			
			|<button id="{search}" text="Search"/>
			|</hbox>
			|<webview id="{searchwebview}" height="400.0" width="1100.0"/>
			|</vbox>
			|</tab>
			|<tab caption="Cmd">
			|<vbox>
			|<hbox>
			|<textfield id="{cmdtext}"/>
			|<button id="{cmdexec}" text="Exec"/>
			|</hbox>
			|<webview id="{cmdwebview}" height="500.0" width="1100.0"/>
			|</vbox>
			|</tab>
			|<tab caption="Settings">
			|<vbox>
			|<hbox>
			|<label text="Editor"/>
			|<filechooser id="{editor}"/>
			|</hbox>
			|<hbox>
			|<label text="Zipper"/>
			|<filechooser id="{zipper}"/>
			|</hbox>
			|</vbox>
			|</tab>
			|<tab caption="Systemlog">
			|<webview id="{systemlog}"/>
			|</tab>
			|<tab caption="Execqueue">
			|<webview id="{execqueue}"/>
			|</tab>
			|<tab caption="Queuelog">
			|<webview id="{execqueuelog}"/>
			|</tab>
			|</tabpane>
			|
			|</vbox>
		""".stripMargin

		MyStage(id="{mainstage}",s=primaryStage,title="appbuilder",blob=blob,handler=handler,
			setstagewidth=true,stagewidth=1200.0,setstageheight=true,stageheight=700)

		Project.clear

		GetMyText("{cmdtext}").SetText("")

		StartCmd
	}

	def Stop
	{
		StopCmd
	}

	def ProjectSavedCallback(path:String)
	{
		Project.set_path(path)

		Project.update
	}

	def SaveProjectAs
	{
		if(!Project.exists)
		{
			Builder.SystemPopUp("Save project error","No project to save.")

			return
		}

		SaveFileAsDialog(
			title="Save project as",
			id="saveprojectas",
			content=Project.toXmlString,
			ext="xml",
			create=false,
			successcallback=ProjectSavedCallback,
			setdirafter="openproject"
		)
	}

	def OpenProject
	{
		val f=ChooseFile("openproject",setdirafter="saveprojectas")

		if(f==null) return

		if(f.exists())
		{
			val path=f.getAbsolutePath()

			Project.set_path(path)

			Project.open
		}
	}

	case class LogItem(line:String,out:Boolean=true)
	{
		def ReportHTMLTableRow:String =
		{			
			s"""
				|<tr>
				|<td>
				|<font color="${if(out) "blue" else "red"}">
				|<pre>$line</pre>
				|</fonr>
				|</td>
				|</tr>
			""".stripMargin
		}
	}

	var cmdlogbuff = scala.collection.mutable.ArrayBuffer[LogItem]() 

	def CmdLog(li:LogItem)
	{
		this.synchronized
		{
			cmdlogbuff+=li
			if(cmdlogbuff.length>200) cmdlogbuff.remove(0)

			val content=s"""
				|<table>
				|${(for(li <- cmdlogbuff.reverse) yield li.ReportHTMLTableRow).mkString("\n")}
				|</table>
			""".stripMargin

			MyActor.queuedExecutor ! ExecutionItem(client="CmdLog",code=new Runnable{def run{
				Builder.LoadWebContent("{cmdwebview}",content)
			}})		
		}
	}

	var cmdprocess:Process=null
	var cmdin:java.io.InputStream=null
	var cmdout:java.io.OutputStream=null
	var cmdreadthread:Thread=null

	def ProcessCmdOut(line:String)
	{
		val effline=line.replaceAll("\\r$","")

		CmdLog(LogItem(effline))
	}

	def CreateCmdReadThread:Thread =
	{
		val truethis=this
		new Thread(new Runnable{def run{
			var buffer=""
			while (!Thread.currentThread().isInterrupted()){
			{
				try
				{
					val chunkobj=cmdin.read()
					try
					{ 
						val chunk=chunkobj.toChar
						if(chunk=='\n')
						{						
							ProcessCmdOut(buffer)
							buffer=""
						} else {
							buffer+=chunk
						}
					}
					catch
					{
						case e: Throwable => 
						{
							val emsg=s"cmd read not a char exception, chunk: $chunkobj"
							println(emsg)
						}
					}
				}
				catch
				{
					case e: Throwable =>
					{
						val emsg=s"cmd read IO exception"
						println(emsg)
					}
				}
			}
		}}})
	}

	def IssueCommand(command:String)
	{
		if(command==null) return		

		val fullcommand=command+"\n"

		try
		{
			cmdout.write(fullcommand.getBytes())
			cmdout.flush()

			CmdLog(LogItem(command,out=false))
		}
		catch
		{
			case e: Throwable =>
			{
				val emsg=s"cmd write IO exception, command: $command"
				println(emsg)
				//e.printStackTrace
			}
		}
	}

	def StartCmd:Boolean =
	{
		val progandargs:List[String] = List("cmd.exe")
		val processbuilder=new ProcessBuilder(progandargs.asJava)

		MyActor.Log("starting cmd process")

		try
		{
			cmdprocess=processbuilder.start()
		}
		catch
		{
			case e: Throwable =>
			{
				MyActor.Log(s"starting cmd process failed")
				return false
			}
		}
		cmdin=cmdprocess.getInputStream()
        cmdout=cmdprocess.getOutputStream()
        cmdreadthread=CreateCmdReadThread
        cmdreadthread.start()

        MyActor.Log("success, cmd started")

        true
	}

	def StopCmd
	{
		if(cmdreadthread!=null)
		{
			cmdreadthread.interrupt()
			cmdreadthread=null
		}

		if(cmdprocess!=null)
		{
			cmdprocess.destroy()
			cmdprocess=null
		}

		cmdin=null
		cmdout=null

		MyActor.Log(s"cmd stopped")
	}

	def CmdExec
	{
		val cmdtext=GetMyText("{cmdtext}")

		val command=cmdtext.GetText

		IssueCommand(command)

		cmdtext.SetText("")
	}

	def SyncGhdesc
	{		
		Project.create_eghdescjson

		val eghdescync=Project.eghdescsyncbat

		StartAsTempBat(eghdescync)
	}

	def CreateRepo
	{
		val createrepo=Project.createrepo

		StartAsTempBat(createrepo)
	}

	def GitFirst
	{
		val githubupfirst=Project.githubfirst(push=false,pause=false)

		StartAsTempBat(githubupfirst)
	}

	def GitUpdate(cm:Boolean=false)
	{
		val githubupdate=Project.githubupdate(push=false,pause=false,cm=cm)

		StartAsTempBat(githubupdate)
	}

	def GitPush
	{
		val gitpush=s"""
			|cd "${Project.folderpath}"
			|git push origin master
			|pause
		""".stripMargin

		StartAsTempBat(gitpush)
	}

	def SetInfo(what:String)
	{
		GetMyText("{infolabel}").SetText(what)
	}

	def ReportEraseFilesResult(efr:EraseFilesResult)
	{
		SetInfo("Removed "+efr.nofiles+" files, "+efr.nodirs+" dirs, total size "+efr.size)
	}

	def ViewRepo
	{
		val url=Project.url

		println(url)

		SelectTab("{maintabpane}","Web")

		LoadWebUrl("{webwebview}",url)
	}

	def CreateBats
	{
		Project.populate_bats
	}

	def RefreshSrc
	{
		Project.refresh_src
	}

	def EditFile(file:String)
	{
		val editor=GS("{components}#{editor}#{path}","")

		val command=s""""$editor" "${DataUtils.DecodeHex(file)}""""

		IssueCommand(command)
	}

	def ZipRepo
	{
		val zipper=GS("{components}#{zipper}#{path}","")

		val zipbat=s"""
			|cd "${Project.rootdir}"
			|"$zipper" a -x!${Project.name}${Project.sep}.git ${Project.name}.7z ${Project.name}
			|pause
		""".stripMargin

		StartAsTempBat(zipbat)
	}

	def StartAsTempBat(what:String)
	{
		DataUtils.WriteStringToFile("stuff/temp.bat",what)

		IssueCommand("start stuff"+DataUtils.Sep+"temp.bat")
	}

	def StartSbt
	{
		val startsbt=s"""
			|cd "${Project.folderpath}"
			|sbt c
		""".stripMargin

		StartAsTempBat(startsbt)
	}

	def SbtRun
	{
		val sbtrun=s"""
			|cd "${Project.folderpath}"
			|sbt run
		""".stripMargin

		StartAsTempBat(sbtrun)
	}

	def Search
	{
		val dir=GS("{components}#{searchdir}","")
		val exts=GS("{components}#{searchexts}","scala")
		val term=GS("{components}#{term}","")

		val extsparts=exts.split("\\+")
		var maxdepth= -1
		if(extsparts.length>1) try
		{
			maxdepth=extsparts(1).toInt
		} catch { case e:Throwable => }

		SelectTab("{maintabpane}","Systemlog")

		AbortDialog("Abort search",() => DataUtils.collectaborted=true)

		Future
		{
			val files=DataUtils.CollectFiles(dir,recursive=true,exts=extsparts(0),term=term,dolog=true,maxdepth=maxdepth)

			val l=dir.length
			var i=0
			val filescontent=(for(file <- files) yield
			{
				i+=1
				val fc=file.substring(l+1)			
				var p=""
				var n=fc
				val ls=fc.lastIndexOf(Project.sep)
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

			MyActor.queuedExecutor ! ExecutionItem(client="Search",code=new Runnable{def run{
				LoadWebContent("{searchwebview}",content)

				CloseAbortDialog

				SelectTab("{maintabpane}","Search")
			}})									
		}
	}

}