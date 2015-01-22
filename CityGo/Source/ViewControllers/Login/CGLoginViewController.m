//
//  CGLoginViewController.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGLoginViewController.h"
#import "CGViewControllerProtected.h"
#import "OLGhostAlertView.h"
#import "AppDelegate.h"

@interface CGLoginViewController () <PFLogInViewControllerDelegate, PFSignUpViewControllerDelegate>

@end

@implementation CGLoginViewController

- (void)viewDidAppear:(BOOL)animated
{
    [[IQKeyboardManager sharedManager] setEnable:NO];
    // Create the log in view controller
    PFLogInViewController *logInViewController = [[PFLogInViewController alloc] init];
    [logInViewController setDelegate:self]; // Set ourselves as the delegate
    
    [logInViewController setFields:PFLogInFieldsFacebook | PFLogInFieldsTwitter | PFLogInFieldsUsernameAndPassword | PFLogInFieldsLogInButton | PFLogInFieldsSignUpButton |
     PFLogInFieldsPasswordForgotten];
    
    // Create the sign up view controller
    PFSignUpViewController *signUpViewController = [[PFSignUpViewController alloc] init];
    [signUpViewController setDelegate:self]; // Set ourselves as the delegate
    
    // Assign our sign up controller to be displayed from the login controller
    [logInViewController setSignUpController:signUpViewController];
    
    // Present the log in view controller
    [self presentViewController:logInViewController animated:YES completion:NULL];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)showMissingFieldsAlert
{
    [[[STAlertView alloc] initWithTitle:@"Missing Information" message:@"Make sure you fill out all of the information!"
                      cancelButtonTitle:@"Drat!"
                       otherButtonTitle:nil
                      cancelButtonBlock:nil otherButtonBlock:nil] show];
}

#pragma mark -
#pragma mark Login/Sign up delegates

// Sent to the delegate to determine whether the log in request should be submitted to the server.
- (BOOL)logInViewController:(PFLogInViewController *)logInController shouldBeginLogInWithUsername:(NSString *)username password:(NSString *)password
{
    // Check if both fields are completed
    if (username && password && username.length != 0 && password.length != 0)
    {
        return YES; // Begin login process
    }
    
    [self showMissingFieldsAlert];
    
    return NO; // Interrupt login process
}

// Sent to the delegate when a PFUser is logged in.
- (void)logInViewController:(PFLogInViewController *)logInController didLogInUser:(PFUser *)user
{
    // [self dismissViewControllerAnimated:YES completion:nil];
    AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
    appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
}

// Sent to the delegate when the log in attempt fails.
- (void)logInViewController:(PFLogInViewController *)logInController didFailToLogInWithError:(NSError *)error
{
    NSLog(@"Failed to log in... %@", error);
}

// Sent to the delegate to determine whether the sign up request should be submitted to the server.
- (BOOL)signUpViewController:(PFSignUpViewController *)signUpController shouldBeginSignUp:(NSDictionary *)info
{
    BOOL informationComplete = YES;
    
    // loop through all of the submitted data
    for (id key in info)
    {
        NSString *field = [info objectForKey:key];
        if (!field || field.length == 0)
        { // check completion
            informationComplete = NO;
            break;
        }
    }
    
    // Display an alert if a field wasn't completed
    if (!informationComplete)
    {
        [self showMissingFieldsAlert];
    }
    
    return informationComplete;
}

// Sent to the delegate when a PFUser is signed up.
- (void)signUpViewController:(PFSignUpViewController *)signUpController didSignUpUser:(PFUser *)user
{
    AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
    appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
}

// Sent to the delegate when the sign up attempt fails.
- (void)signUpViewController:(PFSignUpViewController *)signUpController didFailToSignUpWithError:(NSError *)error
{
    NSLog(@"Failed to sign up...");
}

@end
