##Course Portal

BY: Bhavana Vennamaneni

PROJECT: Course Portal

DESCRIPTION

This application is developed for both professor and students in different versions. 

Functionality in Professor version:
1. Browse through related courses(About Course, Enrolled Students, Announcements)
2. Access the Enrolled Students Data(by selecting RedId displayed in Enrolled List in CourseActivity)
3. Post Announcements in application/ Send Email
4. Post Grades 

Announcements can be created specific to certain groups based on course & enrollment type.
Grades option supports different grading criteria and provides the professor with the grading criteria related to the course selected.

Functionality in Student version:
1. Browse through enrolled courses(About Course, Grades, Announcements)
2. View specific announcements based on enrollment type in the CourseActivity(Online,Hybrid,All)

Action Bar Icons
1. Settings - Opens Settings Activity(It has options to Enable Security Lock & Remove Account)
2. User - Opens a dialog with user details
3. Refresh - Reloads the data from backend
4. Write Post - Visible only in Professor version, opens PostActivity

Settings options
1. Security Lock - Prompts user to set passcode and prompts to enter passcode every time the app is opened
2. Remove Account - Removes the current user details and security settings and is redirected to Login Activity

WorkFlow:
When the application is installed and launched for first time, it prompts the user to setup his/her account.
User can sign in, if account exists or create a new account in SignUp page. A user should be valid, in order to create an account(i.e, the user data should be available on server).
Once the user logs in, application stores user details and opens the home page from next time directly. Depending on whether the user is student/professor appropriate contents are displayed. 
In Main screen of Course Portal, it displays the courses related to user and 5 recent announcements posted under ALL category from the related courses. 
The user can select a Course under 'My Courses' to view the detailed information.

EXTERNAL LIBRARIES

1. 	Parse SDK - https://www.parse.com/
	
SAMPLE DATA STORED IN BACKEND
	
Existing Users: Can login into app using following credentials

1.  Professor - Roger Whitney
	RedId: 817111000 
	Password: qwerty
	
2.  Student - Bhavana Vennamaneni
	RedId: 817670809
	Password: qwerty
	
Valid User Data: Can create new user Account using following id's

Professor
1. 817000111
2. 817000222
3. 817000333
4. 817000444

Student
1. 817670809
2. 817111222
3. 817111333
4. 817111444



 
