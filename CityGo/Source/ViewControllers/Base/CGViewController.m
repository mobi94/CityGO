//
//  CGViewController.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGViewController.h"
#import "CGViewControllerProtected.h"


@interface CGViewController ()

@end

@implementation CGViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[self navigationItem] setBackBarButtonItem:[[UIBarButtonItem alloc] initWithTitle:@""
                                                                                 style:UIBarButtonItemStylePlain
                                                                                target:nil
                                                                                action:nil]];
}

- (void)didReceiveMemoryWarning
{
    NSLog(@"Memory Warning");
    
    [super didReceiveMemoryWarning];
}

#pragma mark -
#pragma mark Observer

- (void)registerObserver
{
    return;
}

- (void)unregisterObserver
{
    [NOTIFICATION_CENTER removeObserver:_notificationsObserver];
}

#pragma mark -
#pragma mark Error handling

- (void)handleError:(NSError *)error
{
    NSString *errorMessage = nil;
    NSString *otherButtonTitle = nil;
    
    errorMessage = error.description;
    
    [[[STAlertView alloc] initWithTitle:@"Uh oh" message:errorMessage
                      cancelButtonTitle:@"Drat!"
                       otherButtonTitle:otherButtonTitle
                      cancelButtonBlock:nil otherButtonBlock:nil] show];
}

#pragma mark -
#pragma mark HUD

- (void)showHUD
{
    [MBProgressHUD showHUDAddedTo:[self view] animated:YES];
}

- (void)hideHUD
{
    [MBProgressHUD hideHUDForView:[self view] animated:YES];
}

@end
