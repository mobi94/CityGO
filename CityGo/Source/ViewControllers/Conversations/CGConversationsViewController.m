//
//  CGConversationsViewController.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGConversationsViewController.h"

@interface CGConversationsViewController ()

@end

@implementation CGConversationsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    // Get the user from a non-authenticated method
    PFQuery *query = [PFUser query];
    PFUser *userAgain = (PFUser *)[query getObjectWithId:@"TZSYovHFc0"];
    
    NSLog(@"%@", userAgain);
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
