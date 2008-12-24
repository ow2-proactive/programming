import pprint
import urllib

# For easy debug
pp = pprint.PrettyPrinter(indent=4)

API_SUFFIX = "api/python"
  
class Hudson:
    """ An Hudson server """

  
    
    def __init__(self, url):
        self.base_url = url
        self.object = eval(urllib.urlopen(self.base_url + API_SUFFIX).read())
        
    def __str__(self):
        return self.object["url"]
   
    def get_raw_object(self):
        return self.object
        
    def get__jobs(self):
        ret = []
        for job in self.object["jobs"]:
            ret.append(Job(job["url"]))
        return ret
    
    def get_job(self, job_name):
        for job in self.object["jobs"]:
            if job["name"] == job_name:
                return Job(job["url"])
        return None

    def get_view(self, view_name):
        for view in self.object["views"]:
            if view["name"] == view_name:
                return View(view["url"])
        return None
        
    def get_views(self):
        ret = []
        for view in self.object["views"]:
            ret.append(View(view["url"]))
        return ret



class View:
    """ A view """
    def __init__(self, url):
        self.url = url
        self.object = eval(urllib.urlopen(self.url  + API_SUFFIX).read())
    
    def __str__(self):
        return "View " + self.get_name()

    def get_name(self):
        return self.url.split("/")[-1]

    def get__jobs(self):
        ret = []
        for job in self.object["jobs"]:
            ret.append(Job(job["url"]))
        return ret
    
    def get_job(self, job_name):
        for job in self.object["jobs"]:
            if job["name"] == job_name:
                return Job(job["url"])
        return None
    
    
            
class Job:
    """ An Hudson Job"""
    
    def __init__(self, url):
        self.url = url
        self.object = eval(urllib.urlopen(self.url + API_SUFFIX).read())
    
    def __str__(self):
        return "Job " + self.get_name() + " status: " + self.get_status()
    
    def get_name(self):
        return self.object["name"]
    
    def get_color(self):
        return self.object["color"]
    
    def get_status(self):
        def color_to_status(color):
            if color == "blue":
                return "successful"
            elif color == "yellow":
                return "unstable"
            elif color == "red":
                return "failed"
            return color
        return color_to_status(self.get_color())
        
    def get_last_build_number(self): 
        return self.object["lastBuild"]["number"]   
    
    def get_last_build_url(self):
        return self.object["lastBuild"]["url"]   
        
    def get_last_build(self):
        return Build(self.get_last_build_url())
    
    def get_build(self, build="lastBuild"):
        return Build(self.url + str(build) + "/")
    
    def get_url(self):
        return self.object["url"]
    
    def get_first_build_number(self):
        return self.object["firstBuild"]["number"] 
    
    def get_first_build(self):
        return Build(self.get_first_build_url())
    
    
class Build:
    """ A Build """
    
    def __init__(self, url, job = None):        
        self.url = url
        self.object = eval(urllib.urlopen(self.url + API_SUFFIX).read())
        self.job = job           
                   
    def __str__(self):
        return "Build " + self.get_job().get_name() + " " + str(self.get_build_number())
    
    def get_raw_object(self):
        return self.object
    
    def get_build_number(self):
        return self.object["number"]
    
    def get_job(self):
        if self.job == None:
            job_url = self.url
            if job_url.endswith("/"):
                job_url  = job_url.strip("/")
            job_url  = job_url.rpartition("/")[0]
            self.job = Job(job_url + "/")
        return self.job
    
    def is_building(self):
        return self.object["building"]
    
    def get_result(self):
        return self.object["result"]

    def is_success(self):
        return self.get_result() == "SUCCESS"

    def is_unstable(self):
        return self.get_result() == "UNSTABLE"
    
    def is_failure(self):
        return self.get_result() == "FAILURE"
    
    def get_test_report(self):
        if not self.is_failure():
            return TestReport(self.url + "/" + "testReport")
        return None
    
class TestReport:
        """ An Test Report """

        def __init__(self, url):
            self.url = url
            self.object = eval(urllib.urlopen(self.url + API_SUFFIX).read())
            
        def get_fail_count(self):
           return self.object["failCount"]
            
        def get_pass_count(self):
            return self.object["passCount"]
        
        def get_skip_count(self):
            return self.object["skipCount"]
            
        def get_failed(self):
            ret = []
            for ext in self.object["child"]:
                for mid in ext["child"]:
                    for int in mid["child"]:
                        if int["status"] != "PASSED":
                            ret.append(int["className"])
            return ret
        
